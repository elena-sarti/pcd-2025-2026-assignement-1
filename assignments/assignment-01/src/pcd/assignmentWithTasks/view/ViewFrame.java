package pcd.assignmentWithTasks.view;

import pcd.assignmentWithTasks.controller.BoundedBufferImpl;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ViewFrame extends JFrame {
    
    private VisualiserPanel panel;
    private ViewModel model;
    private RenderSynch sync;
    private BoundedBufferImpl<Integer> inputBuffer;
    
    public ViewFrame(ViewModel model, int w, int h, BoundedBufferImpl<Integer> buffer){
    	this.model = model;
    	this.sync = new RenderSynch();
        this.inputBuffer = buffer;
    	setTitle("Poool Game");
        setSize(w,h + 25);
        setResizable(false);
        panel = new VisualiserPanel(w,h);
        getContentPane().add(panel);
        addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent ev){
				System.exit(-1);
			}
			public void windowClosed(WindowEvent ev){
				System.exit(-1);
			}
		});
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {try {
                    inputBuffer.put(e.getKeyCode());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
     
    public void render(){
		long nf = sync.nextFrameToRender();
        panel.repaint();
		try {
			sync.waitForFrameRendered(nf);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
    }
        
    public class VisualiserPanel extends JPanel {
        private int ox;
        private int oy;
        private int delta;
        
        public VisualiserPanel(int w, int h){
            setSize(w,h + 25);
            ox = w/2;
            oy = h/2;
            delta = Math.min(ox, oy);
        }

        public void paint(Graphics g){
    		Graphics2D g2 = (Graphics2D) g;

            FontMetrics fm = g2.getFontMetrics();

    		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
    		          RenderingHints.VALUE_ANTIALIAS_ON);
    		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
    		          RenderingHints.VALUE_RENDER_QUALITY);
    		g2.clearRect(0,0,this.getWidth(),this.getHeight());

    		g2.setColor(Color.LIGHT_GRAY);
		    g2.setStroke(new BasicStroke(1));
    		g2.drawLine(ox,0,ox,oy*2);
    		g2.drawLine(0,oy,ox*2,oy);
    		g2.setColor(Color.BLACK);
    		
    		    g2.setStroke(new BasicStroke(1));
	    		for (var b: model.getBalls()) {
	    			var p = b.pos();
	            	int x0 = (int)(ox + p.x()*delta);
	                int y0 = (int)(oy - p.y()*delta);
	                int radiusX = (int)(b.radius()*delta);
	                int radiusY = (int)(b.radius()*delta);
	                g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
	    		}

                g2.setStroke(new BasicStroke(1));
                for (var h: model.getHoles()) {
                    var p = h.pos();
                    int x0 = (int)(ox + p.x()*delta);
                    int y0 = (int)(oy - p.y()*delta);
                    int radiusX = (int)(h.radius()*delta);
                    int radiusY = (int)(h.radius()*delta);
                    g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
                    g2.fillOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
                }

    		    g2.setStroke(new BasicStroke(3));
	    		var pb = model.getPlayerBall();
	    		if (pb != null) {
					var p1 = pb.pos();
		        	int x0 = (int)(ox + p1.x()*delta);
		            int y0 = (int)(oy - p1.y()*delta);
	                int radiusX = (int)(pb.radius()*delta);
	                int radiusY = (int)(pb.radius()*delta);
	                g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
                    int textWidth = fm.stringWidth("P");
                    int textHeight = fm.getAscent();
                    g2.setColor(Color.PINK);
                    g2.fillOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
                    g2.setColor(Color.BLACK);
                    g2.drawString("P", x0 - (textWidth / 2), y0 + (textHeight / 2));
	    		}

                g2.setStroke(new BasicStroke(3));
                var bb = model.getBotBall();
                if (bb != null) {
                    var b1 = bb.pos();
                    int x0 = (int)(ox + b1.x()*delta);
                    int y0 = (int)(oy - b1.y()*delta);
                    int radiusX = (int)(bb.radius()*delta);
                    int radiusY = (int)(bb.radius()*delta);
                    g2.drawOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
                    int textWidth = fm.stringWidth("B");
                    int textHeight = fm.getAscent();
                    g2.setColor(Color.CYAN);
                    g2.fillOval(x0 - radiusX,y0 - radiusY,radiusX*2,radiusY*2);
                    g2.setColor(Color.BLACK);
                    g2.drawString("B", x0 - (textWidth / 2), y0 + (textHeight / 2));
                }

    		    g2.setStroke(new BasicStroke(1));
	    		g2.drawString("Num small balls: " + model.getBalls().size(), 10, 740);
	    		g2.drawString("Frame per sec: " + model.getFramePerSec(), 10, 760);
                g2.setStroke(new BasicStroke(3));
                g.setFont(new Font("Arial", Font.BOLD, 32));
                g2.drawString("PLAYER SCORE: " + model.getPlayerScore(), 200, 760 );
                g2.drawString("BOT SCORE: " + model.getBotScore(), 700, 760 );


                if (model.isGameOver()) {

                    g2.setColor(new Color(255, 192, 203, 180));
                    g2.fillRect(0, 0, getWidth(), getHeight());

                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 40));

                    String endMessage = model.getEndMessage();

                    FontMetrics fmEnd = g2.getFontMetrics();
                    int x = (getWidth() - fmEnd.stringWidth(endMessage)) / 2;
                    int y = getHeight() / 2;

                    g2.drawString(endMessage, x, y);
                }

	    		sync.notifyFrameRendered();
    		
        }
        
    }
}
