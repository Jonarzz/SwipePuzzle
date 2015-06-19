package com.jonasz.swipepuzzle.shapes;

public class Rectangle {
	
	private Point upperLeft;
	
	private int width;
	private int height;
	
	public Rectangle(int x, int y, int width, int height) {
		upperLeft = new Point(x, y);
		
		this.width = width;
		this.height = height;
	}
	
	public boolean contains(Point point) {
		if ((point.x >= upperLeft.x && point.x <= upperLeft.x + width) && (point.y <= upperLeft.y && point.y >= upperLeft.y + height))
			return true;
		else
			return false;
	}
	
}
