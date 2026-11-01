import java.awt.*;
import java.lang.*;
import java.applet.Applet;

public class Chart extends Applet {
    float   fper[];
    float   fint[];
    int     nbData;
    float   cutoff;
    float maxIntensity = 0;
	float minIntensity = 0;
	String str;
	int maxInt_ind = 0;
    int width   ;
    int height  ;

	public synchronized void init() {
	Dimension s = size();
	width = s.width -50;
	height = s.height-50;	
	    String rs;
	    rs = getParameter("cutoff");
        cutoff = Float.valueOf(rs).floatValue();
        nbData = Integer.parseInt(getParameter("nbdata"));
		fper = new float[nbData];
		fint = new float[nbData];
		str = getParameter("p"+2);
	      	for (int i = 1 ; i <= nbData ; i++) {
		    fper[i-1] = Float.valueOf(getParameter("p"+i)).floatValue();
			fint[i-1] = Float.valueOf(getParameter("i"+i)).floatValue();
			if (fint[i-1] > maxIntensity) maxIntensity = fint[i-1];
			if (fint[i-1] == maxIntensity) maxInt_ind = i-1;
			}
			minIntensity = maxIntensity;
			for ( int i=1 ; i<=nbData ; i++){
			if (fint[i-1] < minIntensity) minIntensity = fint[i-1];   
			}

	}

	
		public synchronized void paint(Graphics g) {
		g.setColor(Color.blue);
		float range = fper[0] - fper[nbData-1];
	
	if (nbData != 1){
				// draw a bar graph	   
				g.setColor(Color.blue);
				if  (cutoff != 0) {
	         		for (int i = 0 ; i < nbData ; i++) {
	           		int x1 = new Float((fper[i]-fper[nbData-1]) / range * width).intValue();
	           		int y1 = height;
	           		int x2 = x1;
	           		int y2 = new Float(height - (fint[i] / maxIntensity * height) ).intValue();
					g.drawLine(  40+x1, (int)(0.9*y1)+20, 40+x2,  (int)(0.9*y2)+20 );
				    }
				//draw a line indicating the cutoff			
					for (int i=37 ; i<= ( width +40); i+=10){
				    g.setColor(Color.red);
					float fff = height* (1-(cutoff/maxIntensity));
					g.drawLine( i, (int)(0.9 * fff)+20, i+5 ,(int)(0.9* fff)+20 );
					}
				g.drawString("cutoff =" + Float.toString(cutoff),width/2,height+25);
				g.setColor(Color.blue);
					 //we use a shift of 40 for x ,-18 for y?

					
			    }
				else { 
		
				// draw a continues graph
					g.setColor(Color.blue);   	
		   			for (int i = 1 ; i < nbData ; i++) {
					int x1 = new Float((fper[i-1]-fper[nbData-1]) / range * width).intValue();
					int y1 = new Float(height - (fint[i-1] / maxIntensity * height) ).intValue();
					int x2 = new Float((fper[i]-fper[nbData-1]) / range * width).intValue();
					int y2 = new Float(height - (fint[i] / maxIntensity * height) ).intValue();
					g.drawLine(  40+x1,  (int)(0.9*y1)+20,  40+x2, (int)(0.9*y2)+20 );
					}
			   }
				// set labels
				g.setColor(Color.black);
				String p = "maximum period found="+Float.toString(fper[0]);
				String u = "minimum period found="+Float.toString(fper[nbData-1]);
				g.drawString( p, width/4 -100,height+25 ); 
	 	        g.drawString( u,3*width/4 +20 ,height+25 ); 
		
				//scale x-axis      
			     double stepx;
			     stepx=0;
				 float scalex ;
				 scalex = width/range ;		
			 	 String a;
				 if (range <=2) {
				 if (range <=1) stepx =0.1;
				  if ((range>1) && (range<=2)) stepx = 0.2 ;
						for (double i=fper[nbData-1] ; i <fper[0]  ; i+=stepx){
						int t = new Float (i*10).intValue();
						float tt;
						tt =Math.round((float)t);
						float ttt ;
						ttt=tt/10;
						a= Float.toString (ttt);	
	    					if (ttt >= fper[nbData-1]) {
							g.setColor(Color.blue);
							g.drawString( a, 32+  (int)(scalex * (ttt-fper[nbData-1]) ) , (int)(0.9*(15+height))+20);
	 						g.setColor(Color.black);
							g.drawLine(  40+(int)(scalex *  (ttt-fper[nbData-1]) ) , (int)(0.9*(-1+height))+20, 40+(int)(scalex *  (ttt-fper[nbData-1]) ), (int)(0.9*(+1+height))+20);
							}	
						}
				 }
		    	else {  
				if ((range>2) && (range<=10)) stepx = 1;
				if ((range>10) && (range<=20)) stepx = 2;
				if ((range>20) && (range<=50)) stepx = 5;
                                if ((range>50) && (range<=100)) stepx = 10;
                                if ((range>100) && (range<=200)) stepx = 20;
                                if ((range>200) && (range<=400)) stepx = 40;
                                if (range>400) stepx = 50;
					for ( int i=(int)fper[nbData-1] ; i <=(int)fper[0]   ; i+=stepx){
				    a= Float.toString ((float)i);	
					g.setColor(Color.blue);
					g.drawString( a, 32+(int)(scalex *  (i-fper[nbData-1]) ) , (int)(0.9*(15+height))+20);
					g.setColor(Color.black);
	 				g.drawLine(  40+(int)(scalex * (i-fper[nbData-1]) ) , (int)(0.9*(-1+height))+20, 40+(int)(scalex * (i-fper[nbData-1]) ), (int)(0.9*(+1+height))+20);
					}	
		
			}	  

			//scale y-axis
			double stepy;
			stepy=0;
			float scaley ;
			scaley = height/maxIntensity;		
			String b;
			if (maxIntensity<1) {
				stepy =0.1;
				for ( float i=0 ; i <  maxIntensity  ; i+=stepy){
				int v = (int) (i* 10);
				b= Float.toString ( (float)v/10  );
				float shifty = height - (int)(scaley * i );
				g.setColor(Color.blue);	
				g.drawString( b, 10, (int)(0.9*(shifty+5))+20);
				g.setColor(Color.black);
				g.drawLine( 37, (int)(0.9 * shifty)+20, 39 , (int)(0.9*shifty)+20);
				}
			}
			else{
				if ((maxIntensity>1) && (maxIntensity<10)) stepy = 1;
				if ((maxIntensity>10) && (maxIntensity<50)) stepy = 5;
				if (maxIntensity>50) stepy = 10;
				for ( float j=0 ; j <= maxIntensity+stepy   ; j+=stepy){
				float shifty = height - (int)(scaley * j );
			    b= Float.toString ( (int)j );	
				g.setColor(Color.blue);	
				g.drawString(b, 10, (int)(0.9* (shifty+5))+20);
				g.setColor(Color.black);	
				g.drawLine(37, (int)(0.9*shifty)+20, 39 , (int)(0.9*shifty)+20);
				}
		    }
			//write the label close to the maximum intensity pick	
			g.setColor(Color.black);
			float ff= fper[maxInt_ind]-fper[nbData-1];
			String w = "(" + Float.toString(fper[maxInt_ind]) + " , " + Float.toString(maxIntensity) + ")";
			if (7+(int)(scalex * ff) < 50){
			    g.drawString( w, 40+(int)(scalex * ff),15);
				}
			else	if (7+(int)(scalex * ff)> width-50){
					g.drawString( w, -25+(int)(scalex * ff) ,15);
					}
					
					else g.drawString( w, 7+(int)(scalex * ff), 15 );			

		} 


	//when only one periodicity is found
	else 
if (cutoff != 0){
	{g.drawString("only one periodicity found",width/2-100,height+25);	
				 g.setColor(Color.blue);
				 float scalex ;
				 scalex = width/2;
			for(  float i=0; i<=2; i++) {
			float ww=  Math.round(fper[0]);  	
			g.setColor(Color.black);
			g.drawString( Float.toString(fper[0]+i-1),32+(int)(scalex*i-12) , (int)(0.9*(15+height))+20);	
			}
			g.setColor(Color.blue);
 			//draw the graph
			g.drawLine( 40+(int)(scalex), (int)(0.9*height)+20, 40+(int)(scalex),  20 );
			g.drawLine(36,20,40,20);
			g.drawString("0.0",10,(int)(0.9*height)+20);
			g.drawLine(36,(int)(0.9*height)+20,40,(int)(0.9*height)+20);

			float scaley ;
			scaley = height/fint[0];		
			g.drawString(Float.toString(fint[0]),10,20);
	
			//write the label close to the maximum intensity	
			g.setColor(Color.black);
			String w = "(" + Float.toString(fper[0]) + " , " + Float.toString(fint[0]) + ")";
			g.drawString( w,40+(int)(scalex)-33 , 15 );
			
				for ( float i=37 ; i<= (width -40); i+=10){
				g.drawLine(  (int)i, (int)(0.9 * ( height- (cutoff/maxIntensity *height)))+20,  (int)(i+5)   ,(int)(0.9* (height- (cutoff/maxIntensity *height) ))+20 );
				}
			 g.drawString("cutoff =" + Float.toString ( cutoff),width/2+70,height+25);
			}
}
else 
		{g.drawString("a continues graph cannot be drawn with one value",width/2-100,height/2);
         g.drawString("enter a non-zero value for the cutoff",width/2-60,height/2+20); 
		}	
			//general 
	    	g.setColor(Color.black);
		    g.drawLine(38,(int)(0.9*height)+20,38,20); // y-axis
		    g.drawLine(38,(int)(0.9*height)+20,42+width,(int)(0.9*height)+20); //x-axis
	     	String per = "p e r i o d i c i t y";
			g.drawString( per, width/2 -20, (int)(0.9*(height +28))+20);
			g.drawString( "i", 1, height/2 -25);
			g.drawString( "n", 0, height/2 -15);
			g.drawString( "t", 1, height/2 -5);
			g.drawString( "e", 0, height/2  +5);
			g.drawString( "n", 0, height/2  +15);
			g.drawString( "s", 0, height/2  +25);
			g.drawString( "i", 1, height/2  +37);
			g.drawString( "t", 1, height/2  +47);
			g.drawString( "y", 0, height/2  +57);
	  }
}
