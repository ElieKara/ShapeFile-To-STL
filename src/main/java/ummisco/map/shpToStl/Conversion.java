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

	public Conversion(int coupe, String hauteur){
		this.coupe=coupe*0.01;
		this.hauteur=hauteur;
		this.gtt = new GeometryToTriangle();
	}


	//Parcours les fichiers shapefiles et stock les Polygons et leur hauteur
	public void parcoursFichier(ArrayList<File> liste_shapefile) throws IOException{
		Map<Geometry,Double> liste_polygon= new HashMap<Geometry,Double>();
		for(int i=0;i<liste_shapefile.size();i++){
			ShpFile file = new ShpFile(liste_shapefile.get(i));
			ArrayList<SimpleFeature> features = file.readFile();
			for(SimpleFeature feature:features){
				Geometry geom = (Geometry) feature.getAttribute("the_geom");
				if(geom instanceof Polygon){
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
				if(geom instanceof MultiPolygon){
					MultiPolygon mp = (MultiPolygon) geom;
					ArrayList<Polygon> listepoly = gtt.decomposeMultiPolygon(mp);
					if(!hauteur.equals("Error")){
						for(Polygon polys:listepoly){
							if(feature.getAttribute(hauteur)!=null)
								liste_polygon.put(polys,(((Number)feature.getAttribute(hauteur)).doubleValue()));
							else{
								hauteur="Error";
								liste_polygon.put(polys,0.0);
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
		regroupePolygon(liste_polygon);
	}


	//Regroupe tous les polygons dans un MultiPolygon puis le met en Geometry
	public void regroupePolygon(Map<Geometry,Double> liste_polygon) throws IOException{
		Polygon[] tab_polys = new Polygon[liste_polygon.keySet().size()];
		int ii = 0;
		for(Geometry p:liste_polygon.keySet()){
			tab_polys[ii] = (Polygon)p;
			ii++;
		}
		GeometryFactory factory = new GeometryFactory();
		Geometry geo = factory.createMultiPolygon(tab_polys);
		decoupeGeometry(geo,liste_polygon);
	}
	

	//Divise la Geometry avec le quadrillage et stock les hauteurs
	public void decoupeGeometry(Geometry geo,Map<Geometry,Double> liste_polygon) throws IOException{
		Map<Geometry, Double> myMap = new HashMap<Geometry,Double>();
		Geometry limite = geo.getEnvelope();
		Coordinate[] coord = limite.getCoordinates();
		ArrayList<Geometry> liste = quadrillage(coord[0],coord[2],coord[1],coupe);
		for(Geometry cell:liste)
			for(Entry<Geometry, Double> current:liste_polygon.entrySet()){
				Geometry res =cell.intersection(current.getKey());
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
		conversionTriangle(myMap,geo,liste);
	}

	
	//Convertit les polygones recupere avec le quadrillage en triangle et les ecrit dans un fichier STL
	public void conversionTriangle(Map<Geometry, Double> decoupe,Geometry geo,ArrayList<Geometry> liste) throws IOException{
		double haut = 0;
		for(int i=0;i<liste.size();i++){	
			Geometry res = geo.intersection(liste.get(i));
			if(res instanceof Polygon){
				Polygon respoly = (Polygon) res;
				haut= hauteurPolygon(decoupe,respoly);
				gtt.polygonSTL(respoly,haut);
			}
			else if(res instanceof MultiPolygon){
				MultiPolygon resmul = (MultiPolygon) res;
				ArrayList<Polygon> listepolys = gtt.decomposeMultiPolygon(resmul);
				for(int j=0;j<listepolys.size();j++){
					haut = hauteurPolygon(decoupe,listepolys.get(j));
					gtt.polygonSTL(listepolys.get(j),haut);
				}
			}
			WriteSTL write = new WriteSTL();
			write.ecrireSTL(gtt.getListeTriangle(), i);
			gtt.videListe();
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

