package ummisco.map.shpToStl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class Conversion {

	private GeometryToTriangle gtt;
	private double coupe;
	private String hauteur;
	private double taille;

	public Conversion(int coupe,int taille, String hauteur){
		this.coupe=coupe*10;
		this.hauteur=hauteur;
		this.taille=taille*10;
		this.gtt = new GeometryToTriangle();
	}


	//Parcours les fichiers shapefiles et decompose les geometry en Polygon et les stock avec leur hauteur
	public void parcoursFichier(ArrayList<File> liste_shapefile) throws IOException{
		Map<Geometry,Double> liste_polygon= new HashMap<Geometry,Double>();
		for(int i=0;i<liste_shapefile.size();i++){
			ShpFile file = new ShpFile(liste_shapefile.get(i));
			ArrayList<SimpleFeature> features = file.readFile();
			for(SimpleFeature feature:features){
				Geometry geom = (Geometry) feature.getAttribute("the_geom");
				if(geom instanceof Polygon){
					if(geom.isValid()){
						Polygon polys = (Polygon) geom;
						if(!hauteur.equals("Error")){
							if(feature.getAttribute(hauteur)!=null)
								liste_polygon.put(polys,(((Number)feature.getAttribute(hauteur)).doubleValue()));
							else{
								hauteur="Error";
								liste_polygon.put(polys,0.0);
							}
						}
						else
							liste_polygon.put(polys,0.0);
					}
				}
				if(geom instanceof MultiPolygon){
					MultiPolygon mp = (MultiPolygon) geom;
					ArrayList<Polygon> listepoly = gtt.decomposeMultiPolygon(mp);
					if(!hauteur.equals("Error")){
						for(Polygon polys:listepoly){
							if(polys.isValid()){
								if(feature.getAttribute(hauteur)!=null)
									liste_polygon.put(polys,(((Number)feature.getAttribute(hauteur)).doubleValue()));
								else{
									hauteur="Error";
									liste_polygon.put(polys,0.0);
								}
							}
						}
					}
					else{
						for(Polygon polys:listepoly){
							liste_polygon.put(polys,0.0);
						}		
					}
				}
			}
		}
		Geometry geo = regroupePolygon(liste_polygon);
		Map<Geometry,Double> new_liste_polygon= new HashMap<Geometry,Double>();
		Geometry limite = geo.getEnvelope();
		Coordinate[] coord = limite.getCoordinates();
		new_liste_polygon = redimensionGeometry(coord[0],coord[2],coord[1],liste_polygon, taille);
		Geometry new_geo = regroupePolygon(new_liste_polygon);
		decoupeGeometry(new_geo,new_liste_polygon);
	}


	//Redimensionne la geometry
	public Map<Geometry,Double> redimensionGeometry(Coordinate min,Coordinate max,Coordinate minmax, Map<Geometry,Double> liste_polygon,double taillle){
		Map<Geometry,Double> new_liste_polygon= new HashMap<Geometry,Double>();
		GeometryFactory fact = new GeometryFactory();
		Point minp = fact.createPoint(min);
		Point maxp = fact.createPoint(max);
		Point minmaxp = fact.createPoint(minmax);
		double mulx = taillle/(minp.distance(minmaxp));
		double muly = taillle/(maxp.distance(minmaxp));
		for(Entry<Geometry, Double> entry : liste_polygon.entrySet()) {
			Coordinate[] coord = entry.getKey().getCoordinates();
			Coordinate[] new_coord = new Coordinate[entry.getKey().getNumPoints()];
			for(int j=0;j<entry.getKey().getNumPoints();j++){
				Coordinate att = new Coordinate();
				att.x=coord[j].x*mulx;
				att.y=coord[j].y*muly;
				new_coord[j]=att;
			}
			new_coord[entry.getKey().getNumPoints()-1]=new_coord[0];
			Geometry geo = fact.createPolygon(new_coord);
			new_liste_polygon.put(geo, entry.getValue());
		}
		return new_liste_polygon;
	}


	//Regroupe tous les polygons valide dans un MultiPolygon puis le met en Geometry
	public Geometry regroupePolygon(Map<Geometry,Double> liste_polygon) throws IOException{
		int ii = 0;
		Polygon[] tab_polys = new Polygon[liste_polygon.keySet().size()];
		for(Geometry p:liste_polygon.keySet()){
			tab_polys[ii] = (Polygon)p;
			ii++;
		}
		GeometryFactory factory = new GeometryFactory();
		Geometry geo = factory.createMultiPolygon(tab_polys);
		return geo;
	}


	//Divise la Geometry avec le quadrillage et l'ecrit le fichier STL
	public void decoupeGeometry(Geometry geo,Map<Geometry,Double> liste_polygon) throws IOException{
		int cpt=0;
		Map<Geometry, Double> myMap = new HashMap<Geometry,Double>();
		Map<Geometry, Double> valide2 = new HashMap<Geometry,Double>();
		Geometry limite = geo.getEnvelope();
		Coordinate[] coord = limite.getCoordinates();
		ArrayList<Geometry> liste = quadrillage(coord[0],coord[2],coord[1],coupe);
		for(Entry<Geometry, Double> current:liste_polygon.entrySet()){
			if(!current.getKey().isValid()){
				ArrayList<Geometry> valide =gtt.decomposePolygon(current.getKey());
				for(int i=0;i<valide.size();i++){
					if(!valide.get(i).isValid()){
						
					}
				}
				for(int i=0;i<valide.size();i++){
					valide2.put(valide.get(i), current.getValue());
				}
			}
			else{
				valide2.put(current.getKey(), current.getValue());
			}
		}
		for(Geometry cell:liste){
			for(Entry<Geometry, Double> current:valide2.entrySet()){
				if(current.getKey().isValid()){
					Geometry res =cell.intersection(current.getKey());
					if(!res.equals(cell)){
					ArrayList<Geometry> tempRes = new ArrayList<Geometry>();
					if(res != null)
						if(res instanceof MultiPolygon){
							MultiPolygon resmul = (MultiPolygon) res;
							ArrayList<Polygon> listepolys = gtt.decomposeMultiPolygon(resmul);
							tempRes.addAll(listepolys);
						}
						else 
							tempRes.add(res);
					for(Geometry g:tempRes)
						myMap.put(g, current.getValue());
					}
				}
			}
			for(Entry<Geometry, Double> entry : myMap.entrySet()){
				gtt.polygonSTL((Polygon)entry.getKey(), entry.getValue(),2.0);
			}
			gtt.polygonSTL((Polygon)cell,2.0,0.0);
			WriteSTL write = new WriteSTL();
			write.ecrireSTL(gtt.getListeTriangle(), cpt);
			gtt.videListe();
			cpt++;
			myMap.clear();
		}	
	}


	//Retourne le quadrillage de la Geometry
	public ArrayList<Geometry> quadrillage(Coordinate min, Coordinate max,Coordinate minmax, double coupe){
		ArrayList<Geometry> quadri = new ArrayList<Geometry>();
		GeometryFactory fact = new GeometryFactory();
		Point minp = fact.createPoint(min);
		Point maxp = fact.createPoint(max);
		Point minmaxp = fact.createPoint(minmax);
		double width = Math.round(((minp.distance(minmaxp))/coupe));
		double height = Math.round(((maxp.distance(minmaxp))/coupe));
		if(width*coupe<minp.distance(minmaxp))
			width++;
		if(height*coupe<maxp.distance(minmaxp))
			height++;
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				Coordinate coord1 = new Coordinate(min.x+coupe*i,min.y+coupe*j);
				Coordinate coord2 = new Coordinate(min.x+coupe*(i+1),min.y+coupe*j);
				Coordinate coord3 = new Coordinate(min.x+coupe*(i+1),min.y+coupe*(j+1));
				Coordinate coord4 = new Coordinate(min.x+coupe*i,min.y+coupe*(j+1));
				Coordinate[] cooord = {coord1,coord2,coord3,coord4,coord1};
				Polygon polys = fact.createPolygon(cooord);
				quadri.add(polys);
			}
		}
		return quadri;
	}


	//Parcours tous les polygons pour retrouver la hauteur du polygon donne
	public double hauteurPolygon(Map<Geometry, Double> decoupe,Polygon polys){
		for(Entry<Geometry, Double> entry : decoupe.entrySet()) {
			if(entry.getKey().equals(polys)){
				return entry.getValue();
			}
		}
		return 0;
	}
}