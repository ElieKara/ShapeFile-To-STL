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

		/*//Cree fichier STL
		FileOutputStream fos = new FileOutputStream("zimbabwe.stl");
		DataOutputStream dos = new DataOutputStream(fos);
		
		//Ecriture du fichier STL
		WriteSTL stl = new WriteSTL(liste_triangle,dos);
		stl.ecrireCommentaire();
		stl.ecrireNbTriangle();
		stl.ecrireTriangles();*/
	}
}
