
package edu.boun.edgecloudsim.utils;


//import java.awt.BorderLayout;
import java.awt.Color;
//import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
import java.util.ArrayList;
//import java.util.LinkedList;
//import java.util.Random;

//import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
//import javax.swing.JPanel;

import edu.auburn.pFogSim.Puddle.Puddle;
import edu.auburn.pFogSim.netsim.*;
import edu.boun.edgecloudsim.core.SimManager;
//import edu.boun.edgecloudsim.edge_server.EdgeHost;
//import edu.boun.edgecloudsim.network.NetworkModel;

//This is testing for a visualization of NetworkTopology
public class LinesComponent extends JComponent{
	private int level;
	public LinesComponent(int _level){
		this.level = _level;
	}
		
	public LinesComponent() {}
	
	private class Line{
	    double x1; 
	    double y1;
	    double x2;
	    double y2;   
	    Color color;
	
	    public Line(double x1, double x2, double x3, double x4, Color color) {
	        this.x1 = x1;
	        this.y1 = x2;
	        this.x2 = x3;
	        this.y2 = x4;
	        this.color = color;
	    }               
	}

	private ArrayList<Line> lines = new ArrayList<Line>();
	public void addLine(double x1, double x2, double x3, double x4) {
	    addLine(x1, x2, x3, x4, Color.black);
	}
	public void addLine(double x1, double x2, double x3, double x4, Color color) {
		//SimLogger.printLine("Before lines.add()");
	    lines.add(new Line(x1,x2,x3,x4, color));        
		//SimLogger.printLine("Before repaint()");
	    repaint();
		//SimLogger.printLine("After repaint()");
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		double hx, hy, cx, cy, c2x, c2y, c3x, c3y;
		double x1, x2, y1, y2;

	    g2d.setColor(Color.red);
	    
	    NetworkTopology networkTopology = ((ESBModel) SimManager.getInstance().getNetworkModel()).getNetworkTopology();
	  /*  for(Link link : networkTopology.getLinks())
	    {
	    	x1 = link.getLeftLink().getKey();
	    	y1 = link.getLeftLink().getValue();
	    	x2 = link.getRightLink().getKey();
	    	y2 = link.getRightLink().getValue();
	    	g2d.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
	    }*/
	    for(Puddle pud : networkTopology.getPuddles())
	    {
	    	/*if(pud.getLevel() == this.level + 1)
	    	{
		    	hx = pud.getHead().getLocation().getXPos();
		    	hy = pud.getHead().getLocation().getYPos();
		    	
		    	for(EdgeHost child : pud.getMembers()) 
		    	{
		    		cx = child.getLocation().getXPos();
		    		cy = child.getLocation().getYPos();
		    		g2d.setColor(Color.GREEN);
		    		g2d.drawLine((int)hx, (int)hy, (int)cx, (int)cy);
		    	}
		    	for(Puddle pud1 : pud.getChildren())
		    	{
		    		if(pud1.getLevel() == this.level)
			    	{
				    	hx = pud1.getHead().getLocation().getXPos();
				    	hy = pud1.getHead().getLocation().getYPos();
				    	
				    	for(EdgeHost child1 : pud1.getMembers()) 
				    	{
				    		cx = child1.getLocation().getXPos();
				    		cy = child1.getLocation().getYPos();
				    		g2d.setColor(Color.ORANGE);
				    		g2d.drawLine((int)hx, (int)hy, (int)cx, (int)cy);
				    	}
				    	for(Puddle pud2 : pud1.getChildren())
				    	{
				    		if(pud2.getLevel() == this.level - 1)
					    	{
						    	hx = pud2.getHead().getLocation().getXPos();
						    	hy = pud2.getHead().getLocation().getYPos();
						    	
						    	for(EdgeHost child2 : pud2.getMembers()) 
						    	{
						    		cx = child2.getLocation().getXPos();
						    		cy = child2.getLocation().getYPos();
						    		g2d.setColor(Color.RED);
						    		g2d.drawLine((int)hx, (int)hy, (int)cx, (int)cy);
						    	}
					    	}
				    	}
			    	}
		    	}
	    	}*/
	    	//Try something else
	    	//Go from heads to heads to heads, don't get any other members
	    	if(pud.getLevel() == this.level + 1)
	    	{
	    		hx = pud.getHead().getLocation().getXPos();
	    		hy = pud.getHead().getLocation().getYPos();
	    		for(Puddle pud1 : pud.getChildren())
	    		{
	    			g2d.setColor(Color.RED);
	    			cx = pud1.getHead().getLocation().getXPos();
	    			cy = pud1.getHead().getLocation().getYPos();
	    			g2d.drawLine((int)hx,  (int)hy,  (int)cx,  (int)cy);
	    			
	    			for(Puddle pud2 : pud1.getChildren())
	    			{
	    				g2d.setColor(Color.ORANGE);
	    				c2x = pud2.getHead().getLocation().getXPos();
	    				c2y = pud2.getHead().getLocation().getYPos();
	    				g2d.drawLine((int)c2x,  (int)c2y, (int)cx, (int)cy);
	    				
	    				for(Puddle pud3 : pud2.getChildren())
	    				{
	    					g2d.setColor(Color.GREEN);
	    					c3x = pud3.getHead().getLocation().getXPos();
	    					c3y = pud3.getHead().getLocation().getYPos();
	    					g2d.drawLine((int)c3x,  (int)c3y,  (int)c2x,  (int)c2y);
	    				}
	    			}
	    		}
	    	}
	    }
	}

	public void drawNetworkTopology(int level) {
	    //Go through layer and find all heads while adding their constituents 
		LinesComponent linescomponent = new LinesComponent(level);
	    
	    JFrame frame = new JFrame("Level " + level + " Topology");
	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.add(linescomponent);
	    frame.setSize(1000, 1000);
	    frame.setLocationRelativeTo(null);
	    frame.setVisible(true);
	}
}
