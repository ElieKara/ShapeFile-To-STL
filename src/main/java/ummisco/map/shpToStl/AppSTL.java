package ummisco.map.shpToStl;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;



public class AppSTL {

	public static void main(String[] args) throws Exception {

		//Cree fichier STL
		FileOutputStream fos = new FileOutputStream("zimbabwe.stl");
		DataOutputStream dos = new DataOutputStream(fos);

		//Ecriture du commentaire du fichier STL (80 octets)
		for(int i=0;i<20;i++){
			dos.writeInt(0);
		}

		//Recuperation des donnees du fichier shp
		ShpFile file = new ShpFile("ne_50m_admin_0_sovereignty.shp");
		ArrayList<SimpleFeature> features = file.readFile();

		//Parcours toute la structure
		for(SimpleFeature feature:features)
		{
			if(feature.getID().equals("ne_50m_admin_0_sovereignty.200")){
				//System.out.println(feature.getAttribute("the_geom"));

				// Verification de la figure geometrique et recuperation des points
				String s = feature.getAttribute("the_geom").toString();
				//if(s.indexOf("GEOMETRYCOLLECTION")!=-1){}
				if(s.indexOf("POINT ZM")!=-1){
					s=s.substring(11,s.length());
				}
				else if(s.indexOf("POINT M")!=-1){
					s=s.substring(10,s.length());
				}else if(s.indexOf("POINT EMPTY")!=-1){
					s=s.substring(14,s.length());
				}else if(s.indexOf("MULTIPOINT")!=-1){
					s=s.substring(13,s.length());
				}else if(s.indexOf("POINT")!=-1){
					s=s.substring(8,s.length());
				}else if(s.indexOf("MULTILINESTRING")!=-1){
					s=s.substring(18,s.length());
				}else if(s.indexOf("LINESTRING")!=-1){
					s=s.substring(13,s.length());
				}else if(s.indexOf("MULTIPOLYGON EMPTY")!=-1){
					s=s.substring(21,s.length());
				}else if(s.indexOf("MULTIPOLYGON")!=-1){
					MultiPolygon mp = (MultiPolygon) feature.getAttribute("the_geom");
					Polygon polys;
					int nb_triangle=0;

					//Ecriture du nombre de triangles dans le fichier STL
					for(int i=0;i< mp.getNumGeometries(); i++){
						polys = ((Polygon)mp.getGeometryN(i));
						nb_triangle=nb_triangle+polys.getNumPoints()-2;
					}
					writeIntLE(dos,nb_triangle);

					//Divise le multipolygon en polygon
					for (int i = 0; i < mp.getNumGeometries(); i++) {
						polys = ((Polygon)mp.getGeometryN(i));
						System.out.println("*******************\n\n\n\ntest polys "+polys);
						polygonSTL(polys, dos);
					}
				}else if(s.indexOf("POLYGON")!=-1){
					s=s.substring(10,s.length());
				}
			}
		}
	}

	//Recupere un triangle qui compose le polygone et l'enleve du polygon
	public static void polygonSTL(Polygon polys, DataOutputStream dos){
		Polygon triangle;
		while(polys.getNumPoints()!=4){
			//System.out.println(polys);
			triangle = trianglePolygon(polys);
			polys=enlevePointPolygon(polys,triangle);			 
			//System.out.println(polys);
			System.out.println("BINGO");
		}
	}
	
	//Enleve un point du polygon
	public static Polygon enlevePointPolygon(Polygon polys, Polygon triangle){
		
		/*Coordinate[] coord_polys=polys.getCoordinates();
		Coordinate[] coord_triangle=triangle.getCoordinates();
		GeometryFactory fact = new GeometryFactory();
		Coordinate[] coord = new Coordinate[polys.getNumPoints()-1];
		int j=0;
		if(coord_polys[0]==coord_triangle[1]){
			for(int i=1;i<polys.getNumPoints()-1;i++){
				coord[j]=coord_polys[i];
				j++;
			}
			coord[polys.getNumGeometries()-1]=coord_polys[1];
		}else{
			for(int i=0;i<polys.getNumPoints();i++){
				if(coord_polys[i].compareTo(coord_triangle[1])==0){
					i++;
				}
				coord[j]=coord_polys[i];
				j++;
			}
		}
		Polygon newpolys =fact.createPolygon(coord);	*/	
		
		System.out.println("before diff " + polys);
		System.out.println("triangle " + triangle);
				System.out.println("diff" +  polys.difference(triangle).getGeometryN(0));
		return (Polygon) polys.difference(triangle);
	}
	
	public static Polygon trianglePolygon(Polygon polys){
		int longueur = polys.getNumPoints();
		if(longueur==4)
			return polys;
		for(int i = 0 ; i< longueur-1;i++)
		{
			for(int j = 2 ; j<longueur - 2 ;j++)
			{
				Polygon triangle;
				int point_debut = i;
				int point_fin = (i+j)%longueur;
				if(orientationPolygon(polys,point_debut,point_fin)){
					triangle=newPolygon(polys,point_debut,point_fin);
				}
				else{
					triangle=newPolygon(polys,point_fin,point_debut);
				}
				if(polys.contains(triangle))
					return trianglePolygon(triangle);	
			}
		}
		return null;
		
	}

	//Recupere un triangle qui est a l'interieur du polygon
	public static Polygon trianglePolygon3(Polygon polys){
		Polygon triangle;
		int point_debut=0;
		int point_fin=2;
		int cpt_test_point=0;

		while(polys.getNumPoints()!=4){
			System.out.println(polys.getNumPoints());
			while(cpt_test_point!=polys.getNumPoints()-4){
				if(orientationPolygon(polys,point_debut,point_fin)){
					triangle=newPolygon(polys,point_debut,point_fin);
				}
				else{
					triangle=newPolygon(polys,point_fin,point_debut);
				}
				System.out.println("DECOUPAGE PAS"+" - "+triangle.getNumPoints() + " " +polys.contains(triangle));
				System.out.println("DECOUPAGE PAS"+" - "+polys);
				System.out.println("DECOUPAGE PAS"+" - "+triangle);
				if(polys.contains(triangle)){
					System.out.println("DECOUPAGE");
					if(triangle.getNumPoints()==4){
						System.out.println("YOUPI2");
						return triangle;
					}
					polys=trianglePolygon(triangle);
					cpt_test_point=polys.getNumPoints()-5;
				}
				System.out.println("DECOUPAGE PAS"+" - "+triangle.getNumPoints());
				cpt_test_point++;
				point_fin++;
			}
			cpt_test_point=0;
			point_debut++;
		}
		System.out.println("YOUPI");
		return polys;
	}

	//Renvoie true si le petit polygon va de A vers B
	public static boolean orientationPolygon(Polygon polys,int pointA, int pointB){
		Coordinate[] coord_polys=polys.getCoordinates();
		int petit,grand,sens1,sens2;
		if(pointA<pointB){
			petit=pointA;
			grand=pointB;
			sens1=grand-petit;
			sens2=coord_polys.length-sens1;
			if(sens1<sens2)
				return true;
			else 
				return false;
		}
		else{
			petit=pointB;
			grand=pointA;
			sens1=grand-petit;
			sens2=coord_polys.length-sens1;
			if(sens1<sens2)
				return false;
			else 
				return true;
		}
	}

	//Divise le polygon en 2 polygon et renvoie le plus petit
	public static Polygon newPolygon(Polygon polys, int pointA, int pointB){
		Coordinate[] coord_polys=polys.getCoordinates();
		GeometryFactory fact = new GeometryFactory();
		int dist = pointB - pointA;
		if(dist<0)
			dist=dist*(-1);
		int mSize = coord_polys.length-dist;
		if(mSize>dist)
			mSize=dist;
		mSize=mSize+2;
		//System.out.println(pointA+" - "+pointB);
		//System.out.println("aaaaaaaaa "+mSize+" - "+coord_polys.length);
		Coordinate[] coords = new Coordinate[mSize];
		int j = 0;
		for(int i = pointA;j<mSize;i++){
			
			if(i%coord_polys.length == coord_polys.length-1)
				i++;
			//System.out.println(coord_polys[i%coord_polys.length]+" - "+i+" - "+i%coord_polys.length);
			coords[j] = coord_polys[i%coord_polys.length];
			j++;
		}
		coords[mSize-1]=coord_polys[pointA];
		Polygon newpolys =fact.createPolygon(coords);
		System.out.println(newpolys);
		return newpolys;
	}

	//Ecrire des float en little endian dans le fichier STL
	public static void writeFloatLE(DataOutputStream out, float value) throws IOException{
		writeIntLE(out,Float.floatToRawIntBits(value));
	}

	//Ecrire des int en little endian dans le fichier STL
	public static void writeIntLE(DataOutputStream out, int value) throws IOException{
		out.writeByte(value & 0xFF);
		out.writeByte((value >> 8) & 0xFF);
		out.writeByte((value >> 16) & 0xFF);
		out.writeByte((value >> 24) & 0xFF);

	}
}