package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class Conversion {

	private ArrayList<File> liste_shapefile;
	private GeometryToTriangle gtt;
	private ArrayList<Triangle> liste_triangle;

	public Conversion(ArrayList<File> liste_shapefile){
		this.liste_shapefile=liste_shapefile;
		this.liste_triangle = new ArrayList<Triangle>();
		gtt = new GeometryToTriangle(liste_triangle);
	}

	
	public void parcoursFichier() throws IOException{
		for(int i=0;i<liste_shapefile.size();i++){
			
			System.out.println("aa");
			//Recuperation des donnees du fichier shp
			ShpFile file = new ShpFile(liste_shapefile.get(i));
			ArrayList<SimpleFeature> features = file.readFile();

			//Parcours toutes la structure
			for(SimpleFeature feature:features){
				System.out.println(feature.getAttributes());
				//Verification de forme geometrique
				String s = feature.getAttribute("the_geom").toString();
				//if(s.indexOf("GEOMETRYCOLLECTION")!=-1){}
				if(s.indexOf("POINT ZM")!=-1){}
				else if(s.indexOf("POINT M")!=-1){
				}else if(s.indexOf("POINT EMPTY")!=-1){
				}else if(s.indexOf("MULTIPOINT")!=-1){
				}else if(s.indexOf("POINT")!=-1){
				}else if(s.indexOf("MULTILINESTRING")!=-1){
				}else if(s.indexOf("LINESTRING")!=-1){
				}else if(s.indexOf("MULTIPOLYGON EMPTY")!=-1){
				}else if(s.indexOf("MULTIPOLYGON")!=-1){
					MultiPolygon mp = (MultiPolygon) feature.getAttribute("the_geom");
					gtt.decomposeMultiPolygon(mp);
				}else if(s.indexOf("POLYGON")!=-1){
					Polygon polys = (Polygon) feature.getAttribute("the_geom");
					gtt.polygonSTL(polys);
				}
			}
		}
		
		//Cree fichier STL
		FileOutputStream fos = new FileOutputStream("zimbabwe.stl");
		DataOutputStream dos = new DataOutputStream(fos);
		
		//Ecrit le fichier STL
		WriteSTL stl = new WriteSTL(liste_triangle,dos);
		stl.ecrireCommentaire();
		stl.ecrireNbTriangle();
		stl.ecrireTriangles();
	}
}

