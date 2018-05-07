package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class Main {

	public static void main(String[] args) throws Exception {
		
		Interface fenetre = new Interface();
		/*ArrayList<Triangle> liste_triangle = new ArrayList<Triangle>();
		GeometryToTriangle gtt = new GeometryToTriangle(liste_triangle);

		//Cree fichier STL
		FileOutputStream fos = new FileOutputStream("zimbabwe.stl");
		DataOutputStream dos = new DataOutputStream(fos);

		//Parcours toute la structure
		for(SimpleFeature feature:features){
			System.out.println(feature.getAttribute("NAME")+" - "+feature.getID());
			if(feature.getID().equals("ne_50m_admin_0_sovereignty.89")){
				
				// Verification de la figure geometrique
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
		
		//Ecriture du fichier STL
		WriteSTL stl = new WriteSTL(liste_triangle,dos);
		stl.ecrireCommentaire();
		stl.ecrireNbTriangle();
		stl.ecrireTriangles();*/
	}
}
