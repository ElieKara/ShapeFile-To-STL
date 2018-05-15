package ummisco.map.shpToStl;

import java.util.ArrayList;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class GeometryToTriangle {
	
	private ArrayList<Triangle> liste_triangle = new ArrayList<Triangle>();
	private ArrayList<Polygon> liste_polygon = new ArrayList<Polygon>();
	
	public GeometryToTriangle(){}


	//Divise le multipolygon en polygon
	public ArrayList<Polygon> decomposeMultiPolygon(MultiPolygon mp){
		Polygon polys;
		for (int i = 0; i < mp.getNumGeometries(); i++) {
			polys = ((Polygon)mp.getGeometryN(i));
			liste_polygon.add(polys);
		}
		return liste_polygon;
	}
	

	//Divise le multipolygon en polygon (quadrillage)
		public ArrayList<Polygon> decomposeMultiPolygonQuadra(MultiPolygon mp){
			Polygon polys;
			ArrayList<Polygon> liste_polygonqua = new ArrayList<Polygon>();
			for (int i = 0; i < mp.getNumGeometries(); i++) {
				polys = ((Polygon)mp.getGeometryN(i));
				liste_polygonqua.add(polys);
			}
			return liste_polygonqua;
		}

	
	//Recupere tous les triangles qui composent le polygon et les convertie en Triangle
	public void polygonSTL(Polygon polys){
		//epaisseurTriangle(polys);
		ArrayList<Polygon> triangles = new ArrayList<Polygon>();
		triangles = trianglePolygon(polys,triangles);
		for(Polygon p:triangles){
			Point3D[] point = new Point3D[3];
			//Point3D[] point2 = new Point3D[3];
			Coordinate[] coord_triangle=p.getCoordinates();
			for(int i=0;i<3;i++){
				point[i] = new Point3D((float) coord_triangle[i].x,0.0f,(float) coord_triangle[i].y);
				//point2[i] = new Point3D((float) coord_triangle[i].x,1.0f,(float) coord_triangle[i].y);
			}
			Triangle tri = new Triangle(point);
			//Triangle tri2 = new Triangle(point2);
			liste_triangle.add(tri);
			//liste_triangle.add(tri2);
		}
	}
	
	
	//Construit les triangles pour l'épaisseur
	public void epaisseurTriangle(Polygon polys){
		for(int i=0;i<polys.getNumPoints()-1;i++){
			Point3D[] point = new Point3D[3];
			Point3D[] point2 = new Point3D[3];
			Coordinate[] coord_polys=polys.getCoordinates();
			point[0]= new Point3D ((float) coord_polys[i].x,0.0f,(float) coord_polys[i].y);
			point2[0]= new Point3D ((float) coord_polys[i].x,1.0f,(float) coord_polys[i].y);
			point[1]= new Point3D ((float) coord_polys[i+1].x,0.0f,(float) coord_polys[i+1].y);
			point2[1]= new Point3D ((float) coord_polys[i+1].x,1.0f,(float) coord_polys[i+1].y);
			point[2]= point2[1];
			point2[2]= point[0];
			Triangle tri = new Triangle(point);
			Triangle tri2 = new Triangle(point2);
			liste_triangle.add(tri);
			liste_triangle.add(tri2);
		}
	}
	
	
	/*//Recupere tous les triangles de la geometrie
	public ArrayList<Polygon> trianglePolygon(Polygon polys){
		ArrayList<Polygon> allTriangle = new ArrayList<Polygon>();
		return trianglePolygon(polys,allTriangle);
	}*/	
	
	
	//Trouve une oreille la stock dans liste des triangles et la soustrait au polygone du debut
	private ArrayList<Polygon> trianglePolygon(Polygon polys,ArrayList<Polygon> allTriangle){
		int longueur = polys.getNumPoints();
		if(longueur == 4 ){
			allTriangle.add(polys);
			return allTriangle;
		}
		Coordinate[] coord_polys=polys.getCoordinates();
		for(int i = 0 ; i< longueur;i++){
			Polygon triangle = generateTriangle(coord_polys[i],coord_polys[(i+1)%longueur],coord_polys[(i+2)%longueur]);
			if(isHear(polys,triangle)){
				allTriangle.add(triangle);
				polys = (Polygon)polys.difference(triangle);
				return trianglePolygon(polys, allTriangle);
				}
		}
		return allTriangle;
	}	
	
	
	//Verifie si le triangle est une oreille
	public boolean isHear(Polygon polys,Polygon triangle ){
		if(!polys.contains(triangle))
			return false;
		Geometry geom = polys.difference(triangle);
		if(geom instanceof Polygon)
			return true;
		return false;
	}
	
	
	//Genere un triangle avec 3 coordonnees
	public Polygon generateTriangle(Coordinate a, Coordinate b, Coordinate c){
		GeometryFactory fact = new GeometryFactory();
		Coordinate[] coords = {a,b,c,a};
		Polygon newpolys =fact.createPolygon(coords);
		return newpolys;
	}
	
	
	//Vide la liste des triangles
	public void videListe(){
		liste_triangle.clear();
	}
	
	
	//Renvoie la liste des triangles
	public ArrayList<Triangle> getListeTriangle(){
		return liste_triangle;
	}
}