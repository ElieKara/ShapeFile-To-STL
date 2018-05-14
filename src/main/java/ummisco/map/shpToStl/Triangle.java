package ummisco.map.shpToStl;

public class Triangle {
	
	Point3D[] points;
	
	public Triangle(Point3D[] point) {
		this.points = point;
	}


	//Retourne un tableau de Point3D qui sont les coordonnees du troangle
	public Point3D[] getPoint3D(){
		return points;
	}
	
	/*
	Byte[] toSTL(){
		Byte[] res = new Byte[50];
		for(int i =0; res.length>i; i++)
			res[i] = 0;
		pushPoint(res, points[0], 0);
		pushPoint(res, points[1], 1);
		pushPoint(res, points[2], 2);
		return res;
	}
	
	private void pushPoint(Byte[] data, Point3D p, int id){
		int index = 12;
		int myIndex = id*12 + index;
		data[myIndex] = (byte)((int)(p.getX()) & 0xFF);
		data[myIndex+1] = (byte)((int)(p.getX()) >> 8 & 0xFF);
		data[myIndex+2] = (byte)((int)(p.getX()) >> 16 & 0xFF);
		data[myIndex+4] = (byte)((int)(p.getX()) >> 24 & 0xFF);

		data[myIndex+5] = (byte)((int)(p.getY()) & 0xFF);
		data[myIndex+6] = (byte)((int)(p.getY()) >> 8 & 0xFF);
		data[myIndex+7] = (byte)((int)(p.getY()) >> 16 & 0xFF);
		data[myIndex+8] = (byte)((int)(p.getY()) >> 24 & 0xFF);

		data[myIndex +9] = (byte)((int)(p.getZ()) & 0xFF);
		data[myIndex+10] = (byte)((int)(p.getZ()) >> 8 & 0xFF);
		data[myIndex+11] = (byte)((int)(p.getZ()) >> 16 & 0xFF);
		data[myIndex+12] = (byte)((int)(p.getZ()) >> 24 & 0xFF);
	}*/
	
}
