package org.jlab.calib;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import org.jlab.clas.detector.BankType;
import org.jlab.clas.detector.DetectorBankEntry;
import org.jlab.clas.detector.DetectorCollection;
import org.jlab.clas.detector.DetectorDescriptor;
import org.jlab.clas.detector.DetectorType;
import org.jlab.clas.tools.benchmark.ProgressPrintout;
import org.jlab.clas.tools.utils.DataUtils;
import org.jlab.clas12.basic.IDetectorProcessor;
import org.jlab.clas12.calib.DetectorShape2D;
import org.jlab.clas12.calib.DetectorShapeTabView;
import org.jlab.clas12.calib.DetectorShapeView2D;
import org.jlab.clas12.calib.IDetectorListener;
import org.jlab.clas12.detector.EventDecoder;
import org.jlab.clas12.detector.FADCConfigLoader;
import org.jlab.clas12.detector.FADCConfig;
import org.jlab.clasrec.main.DetectorEventProcessorDialog;
import org.jlab.data.io.DataEvent;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.jlab.evio.clas12.EvioSource;
import org.jlab.geom.prim.Point3D;
import org.root.attr.ColorPalette;
import org.root.attr.TStyle;
import org.root.func.F1D;
import org.root.group.TBrowser;
import org.root.group.TDirectory;
//import org.root.histogram.MyGraphErrors;
//import org.root.histogram.MyH1D;
//import org.root.histogram.MyH2D;
import org.root.pad.TEmbeddedCanvas;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

/**
 *
 * @author gavalian
 * @edited by N. Compton & Taya C.
 */
public class PCALcalibv2 extends JFrame implements IDetectorListener, IDetectorProcessor, ActionListener {

    //public EventDecoder     decoder = new EventDecoder();
	//FADCConfigLoader          fadc  = new FADCConfigLoader();
    
	PCALDrawDB pcal = new PCALDrawDB();
    DetectorShapeTabView  view   = new DetectorShapeTabView();
    DetectorShapeView2D  dv2,dv3,dv4,dv5,dv6,dv7,dv8,dv9;
    public CanvasViewPanel        canvasView;
	public TEmbeddedCanvas         canvas,canvas1,canshape;
	
    
    int nProcessed = 3000000;

    // ColorPalette class defines colors
    ColorPalette         palette   = new ColorPalette();
    int numsectors = 1;
    int numpaddles = 68 * 10000 + 62 * 100 + 62;
    
    //private String inputFileName = "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/fc-muon-3M-s2.evio";
    //private String inputFileName = "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.0.evio";
    private String inputFileName[] = {"/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.0.evio",
    								  "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.1.evio",
    								  "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.2.evio",
    								  "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.3.evio",
    								  "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.4.evio",
    								  "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.5.evio",
    								  "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.6.evio",
    								  "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.7.evio",
    								 };
    //sector2_000251_mode7.evio.0
    //fc-muon-500k-s2-noatt.evio 
    //fc-muon-3M-s2.evio  "sector 2"
    //fc-muon-500k.evio "sector 5"
    //private int RunNumber = 4284; 
    private int CurrentSector = 3; 
    private static int iteration = 0;
    private int numiterations = 1;
    private int countertest = 0;
    
    TDirectory mondirectory = new TDirectory("calibration");
    
    int hit[][][] = new int[68][62][62]; //bad pixel, good pixel, maybe good = 0, 1, 2
    
	double udpixel[][][] = new double[68][62][62]; //u-PMT distance with that pixel
	double vdpixel[][][] = new double[68][62][62]; //v-PMT distance with that pixel
	double wdpixel[][][] = new double[68][62][62]; //w-PMT distance with that pixel
	
	double uwidth[][][] = new double[68][62][62]; //u Gaussian width pixel
	double vwidth[][][] = new double[68][62][62]; //v Gaussian width pixel
	double wwidth[][][] = new double[68][62][62]; //w Gaussian width pixel
	
	double ucent[][][] = new double[68][62][62]; //u Gaussian centroid pixel
	double vcent[][][] = new double[68][62][62]; //v Gaussian centroid pixel
	double wcent[][][] = new double[68][62][62]; //w Gaussian centroid pixel
	
	int pixelnumber[][][] = new int[68][62][62]; //indexing of the pixel number
	
	MyH1D Hpixarea = new MyH1D("pixarea",20,0.0,100.0);
	MyH1D Hpixcounts = new MyH1D("pixcounts",1000,0.0,1000.0);
	//int loop3Dhist[][][][] = new int[3000][50][50][50]; //indexing of 4D histogram
	//ArrayList<MyH3D> pixeladclist;
	//MyH4D H4ADC = new MyH4D("ADCvalues",50,0.0,150.0,50,0.0,150.0,50,0.0,150.0,3000,0.5,6500.5);
	
	double ugain[] = new double[68]; //
	double vgain[] = new double[62]; //
	double wgain[] = new double[62]; //
	
	double umaxX[] = new double[68]; //
	double vmaxX[] = new double[62]; //
	double wmaxX[] = new double[62]; //
	
	double uA[] = new double[68]; //
	double uB[] = new double[68]; //
	double uC[] = new double[68]; //
	
	double genuA[] = new double[68]; //
	double genuB[] = new double[68]; //
	double genuC[] = new double[68]; //
	
	double vA[] = new double[62]; //
	double vB[] = new double[62]; //
	double vC[] = new double[62]; //
	
	double genvA[] = new double[62]; //
	double genvB[] = new double[62]; //
	double genvC[] = new double[62]; //
	
	double wA[] = new double[62]; //
	double wB[] = new double[62]; //
	double wC[] = new double[62]; //
	
	double genwA[] = new double[62]; //
	double genwB[] = new double[62]; //
	double genwC[] = new double[62]; //
	
	
    public TDirectory getDir(){
        return this.mondirectory;
    }

    public PCALcalibv2(){
        super();
        //fadc.load("/test/fc/fadc",10,"default");
        this.initDetector();
        this.initCanvases();
        //this.initHistograms();
        this.setLayout(new BorderLayout());
        JSplitPane  splitPane = new JSplitPane();
        splitPane.setLeftComponent(this.view);
        splitPane.setRightComponent(this.canvasView);
        this.add(splitPane,BorderLayout.CENTER);
        //JPanel buttons = new JPanel();
        //JButton process = new JButton("Process");
        //buttons.setLayout(new FlowLayout());
        //buttons.add(process);
        //process.addActionListener(this);
        //this.add(buttons,BorderLayout.PAGE_END);
        this.pack();
        this.setVisible(true);
    }
    
    public void initCanvases(){
    	canvas     = new TEmbeddedCanvas();
		canvas1    = new TEmbeddedCanvas();
		canshape   = new TEmbeddedCanvas();
		canvasView = new CanvasViewPanel();
		canvasView.addCanvasLayer("Attenuation",canvas);
		canvasView.addCanvasLayer("Average Energy",canvas1);
		canvasView.addCanvasLayer("Shape Information",canshape);
    }
    
    /**
     * Creates a detector Shape.
     */
    public void initDetector(){

    	int sector = 0;
    	int count = 0;
    	String name;
    	//pixeladclist = new ArrayList<MyH3D>(68*62*62);
    	//pixeladclist = new ArrayList<MyH3D>(7000);
    	//draw pixels
    	dv2 = new DetectorShapeView2D("PCAL Pixels");
    	for(int upaddle = 0; upaddle < 68; upaddle++){
        	for(int vpaddle = 0; vpaddle < 62; vpaddle++){
        		for(int wpaddle = 0; wpaddle < 62; wpaddle++){
        			if(pcal.isValidPixel(sector, upaddle, vpaddle, wpaddle))
        			{
        				DetectorShape2D shape = pcal.getPixelShape(CurrentSector, upaddle, vpaddle, wpaddle);
        				for(int i = 0; i < shape.getShapePath().size(); ++i)
        				{
        					shape.getShapePath().point(i).set(shape.getShapePath().point(i).x(), shape.getShapePath().point(i).y(), 0.0);
        				}
        				dv2.addShape(shape);
        				Hpixarea.fill(pcal.shapeArea(shape));
        				hit[upaddle][vpaddle][wpaddle] = 1;
        				
        				
        				//System.out.println(Runtime.getRuntime().freeMemory());
        				//System.out.println(pixeladclist.size());
        				/*name = String.format("ADC_%02d_%02d_%02d", upaddle, vpaddle, wpaddle);
        				pixeladclist.add(new MyH3D(name,50,0.0,150.0,50,0.0,150.0,50,0.0,150.0));
        				*/
        				pixelnumber[upaddle][vpaddle][wpaddle] = count;
        				++count;
        				
        				udpixel[upaddle][vpaddle][wpaddle] = pcal.getUPixelDistance(upaddle, vpaddle, wpaddle);
        				vdpixel[upaddle][vpaddle][wpaddle] = pcal.getVPixelDistance(upaddle, vpaddle, wpaddle);
        				wdpixel[upaddle][vpaddle][wpaddle] = pcal.getWPixelDistance(upaddle, vpaddle, wpaddle);
        			}
        			else
        			{
        				hit[upaddle][vpaddle][wpaddle] = 0;
        				pixelnumber[upaddle][vpaddle][wpaddle] = -10;
        				udpixel[upaddle][vpaddle][wpaddle] = -10.0;
        				vdpixel[upaddle][vpaddle][wpaddle] = -10.0;
        				wdpixel[upaddle][vpaddle][wpaddle] = -10.0;
        			}
        		}
        	}
        }
    	this.view.addDetectorLayer(dv2);
    	view.addDetectorListener(this);
    	//pixeladclist.trimToSize();
    	//System.out.println(pixeladclist.size());
    	
    	//draw UW pane
    	dv3 = new DetectorShapeView2D("PCAL UW");
    	dv3 = pcal.drawUW(CurrentSector);
    	this.view.addDetectorLayer(dv3);
    	view.addDetectorListener(this);
    	
    	//draw UV pane
    	dv4 = new DetectorShapeView2D("PCAL VU");
    	dv4 = pcal.drawVU(CurrentSector);
    	this.view.addDetectorLayer(dv4);
    	view.addDetectorListener(this);
    	
    	//draw UW pane
	    dv5 = new DetectorShapeView2D("PCAL WU");
	    dv5 = pcal.drawWU(CurrentSector);
	    this.view.addDetectorLayer(dv5);
	    view.addDetectorListener(this);

    	
    	
    	//draw U strips
    	dv7 = new DetectorShapeView2D("PCAL U Strips");
    	dv7 = pcal.drawUStrips(CurrentSector);
    	this.view.addDetectorLayer(dv7);
    	view.addDetectorListener(this);

    	
    	//draw V strips
    	dv8 = new DetectorShapeView2D("PCAL V Strips");
    	dv8 = pcal.drawVStrips(CurrentSector);
    	this.view.addDetectorLayer(dv8);
    	view.addDetectorListener(this);
 
    	//draw W strips
    	dv9 = new DetectorShapeView2D("PCAL W Strips");
    	dv9 = pcal.drawWStrips(CurrentSector);
    	this.view.addDetectorLayer(dv9);
    	view.addDetectorListener(this);
    	
    	
    }
    
    /**
     * When the detector is clicked, this function is called
     * @param desc 
     */
    public void detectorSelected(DetectorDescriptor desc){
        int u, v, w, uvwnum;
        String name, name2;
        String namedir;
        MyH1D h1 = null;
        MyH1D h2 = null;
        MyH1D h3 = null;
        MyH1D hsum1 = null;
        MyH1D hsum2 = null;
        MyH1D hsum3 = null;
        F1D gaus = null;
        F1D exp = null;
        F1D genexp = null;
        F1D genexp1 = null;
        F1D genexp2 = null;
        MyGraphErrors g1;
        
        Object[] obj = new Object[3];
        double[] xvert = new double[20];
        double[] xverte = new double[20];
        double[] yvert = new double[20];
        double[] yverte = new double[20];
        int size, counts;
        MyGraphErrors gshape;
        double area, ratio;
        
        TStyle.setStatBoxFont("Helvetica", 12);
            
        
        //System.out.println("SELECTED = " + desc);
        
        
        if(desc.getLayer() == 2) //pixel pane
        {
        	//canvas0
        	canvas.divide(2,3);
	        uvwnum = (int)desc.getComponent();
	    	u = (int)(uvwnum/10000.0);
	    	uvwnum -= u*10000;
	    	v = (int)(uvwnum/100.0);
	    	uvwnum -= v*100;
	    	w = uvwnum;
	    	
	    	namedir = String.format("attendir%02d", iteration-1);
	    	name = String.format("attu_%02d", u + 1);
		    g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
		    g1.setMarkerSize(2);
		    canvas.cd(0);
		    canvas.draw(g1);
		    
		    name = String.format("attufit_%02d", u + 1);
		    exp  = (F1D)getDir().getDirectory(namedir).getObject(name);
		    canvas.draw(exp,"same");
		    
		    genexp = new F1D("exp+p0",0.0,umaxX[u]);
		    genexp.setParameter(0,genuA[u]);
		    genexp.setParameter(1,genuB[u]);
		    genexp.setParameter(2,genuC[u]);
		    genexp.setLineColor(2);
		    genexp.setLineStyle(2);
		    canvas.draw(genexp,"same");
	        
		    namedir = String.format("crossStripHisto%02d", iteration - 1);
		    name = String.format("adu_sig");
			name2 = String.format("adu_%02d_%02d_%02d", u + 1, v + 1, w + 1);
	        h1  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, v, v, w, w);
	        canvas.cd(1);
	        canvas.draw(h1);
	        

	        
	        namedir = String.format("attendir%02d", iteration-1);
	        name = String.format("attv_%02d", v + 1);
	        g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
	        g1.setMarkerSize(2);
	        canvas.cd(2);
	        canvas.draw(g1);
	        
	        genexp1 = new F1D("exp+p0",0.0,vmaxX[v]);
		    genexp1.setParameter(0,genvA[v]);
		    genexp1.setParameter(1,genvB[v]);
		    genexp1.setParameter(2,genvC[v]);
		    genexp1.setLineColor(2);
		    genexp1.setLineStyle(2);
		    canvas.draw(genexp1,"same");
	        
	        
		    namedir = String.format("crossStripHisto%02d", iteration - 1);
		    name = String.format("adv_sig");
			name2 = String.format("adv_%02d_%02d_%02d", u + 1, v + 1, w + 1);
			h1 = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, v, v, w, w);
	        canvas.cd(3);
	        canvas.draw(h1);
	        
	        
	        namedir = String.format("attendir%02d", iteration-1);
	        name = String.format("attw_%02d", w + 1);
	        g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
	        g1.setMarkerSize(2);
	        canvas.cd(4);
	        canvas.draw(g1);
	        
	        genexp2 = new F1D("exp+p0",0.0,wmaxX[w]);
		    genexp2.setParameter(0,genwA[w]);
		    genexp2.setParameter(1,genwB[w]);
		    genexp2.setParameter(2,genwC[w]);
		    genexp2.setLineColor(2);
		    genexp2.setLineStyle(2);
		    canvas.draw(genexp2,"same");
	        
		    namedir = String.format("crossStripHisto%02d", iteration - 1);
		    name = String.format("adw_sig");
			name2 = String.format("adw_%02d_%02d_%02d", u + 1, v + 1, w + 1);
			h1 = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, v, v, w, w);
	        canvas.cd(5);
	        canvas.draw(h1);
	        
	        //canvas1
	        canvas1.divide(2,2);
	        canvas1.cd(0);
	        namedir = String.format("pixelsignal%02d", iteration-1);
	        name = String.format("toten_%02d_%02d_%02d", u + 1, v + 1, w + 1);
	        h1  = (MyH1D)getDir().getDirectory(namedir).getObject(name);
	        canvas1.draw(h1);
	        
	        canvas1.cd(1);
	        namedir = String.format("pixelsignal%02d", iteration-1);
	        name = String.format("Uen_%02d_%02d_%02d", u + 1, v + 1, w + 1);
	        hsum1  = (MyH1D)getDir().getDirectory(namedir).getObject(name);
	        hsum1.setLineColor(2);
	        canvas1.draw(hsum1,"same");
	        
	        canvas1.cd(2);
	        namedir = String.format("pixelsignal%02d", iteration-1);
	        name = String.format("Ven_%02d_%02d_%02d", u + 1, v + 1, w + 1);
	        hsum2  = (MyH1D)getDir().getDirectory(namedir).getObject(name);
	        hsum2.setLineColor(3);
	        canvas1.draw(hsum2,"same");
	        
	        canvas1.cd(3);
	        namedir = String.format("pixelsignal%02d", iteration-1);
	        name = String.format("Wen_%02d_%02d_%02d", u + 1, v + 1, w + 1);
	        hsum3  = (MyH1D)getDir().getDirectory(namedir).getObject(name);
	        hsum3.setLineColor(4);
	        canvas1.draw(hsum3,"same");
	        
	        
	       //////////// Shape Information //////////////////////////
	        DetectorShape2D pixshape = dv2.getSelectedShape();
	        size = pixshape.getShapePath().size();
	        for(int i=0; i < size;++i)
	        {
	        	xvert[i] = pixshape.getShapePath().point(i).x();
	        	yvert[i] = pixshape.getShapePath().point(i).y();
	        	xverte[i] = 0.0;
	        	yverte[i] = 0.0;
	        }
	        
	        canshape.divide(2,2);
	        canshape.cd(0);
	        gshape = graphn("shapeoutline", size, xvert, yvert, xverte, yverte);
	        canshape.draw(gshape);
	        
	        
	        canshape.cd(1);
	        h1  = (MyH1D)this.Hpixarea;
	        canshape.getPad().setAxisRange(0.0, 50.0, 0.0, 1.1 * h1.getBinContent(h1.getMaximumBin()));
	        canshape.draw(h1);
	        hsum1  = new MyH1D("thisarea", 200, 0.0, 1000.0);
	        area = pcal.shapeArea(pixshape);
	        //System.out.println("area: " + area);
	        hsum1.fill(area,h1.getBinContent((int)(area/(100.0/200.0))));
	        hsum1.setLineColor(2);
	        canshape.draw(hsum1,"same");
	        
	        
	        canshape.cd(2);
	        h2  = (MyH1D)this.Hpixcounts;
	        canshape.getPad().setAxisRange(0.0, 500.0, 0.0, 1.1 * h2.getBinContent(h2.getMaximumBin()));
	        canshape.draw(h2);
	        namedir = String.format("crossStripHisto%02d", iteration - 1);
		    name = String.format("adu_sig");
			name2 = String.format("adu_%02d_%02d_%02d", u + 1, v + 1, w + 1);
	        hsum2  = new MyH1D("thiscount", 1000, 0.0, 1000.0);
	        counts = ((MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, v, v, w, w)).getEntries();
	        hsum2.fill(counts,h2.getBinContent(counts));
	        hsum2.setLineColor(2);
	        canshape.draw(hsum2,"same");
	        
	        
	        canshape.cd(3);
	        hsum3  = new MyH1D("thisratio", 200, 0.0, 1000.0);
	        hsum3.fill((double)counts/area);
	        hsum3.setLineColor(2);
	        canshape.getPad().setAxisRange(0.0, 50.0, 0.0, 2.0);
	        canshape.draw(hsum3);
	        
			
        }
        if(desc.getLayer() == 3) //UW
        {
        	//this.canvas = new EmbeddedCanvas();
        	//this.canvas.update();
	        canvas.divide(2,2);
	        uvwnum = (int)desc.getComponent();
	    	u = (int)(uvwnum/100.0);
	    	uvwnum -= u*100;
	    	w = uvwnum;
	    	v = 61;
	    	
//	    	System.out.println("umaxX[u]: " + umaxX[u]);
//	    	System.out.println("genuA[u]: " + genuA[u]);
//	    	System.out.println("genuB[u]: " + genuB[u]);
//	    	System.out.println("genuC[u]: " + genuC[u]);
//	    	
//	    	System.out.println("wmaxX[w]: " + umaxX[w]);
//	    	System.out.println("genwA[w]: " + genwA[w]);
//	    	System.out.println("genwB[w]: " + genwB[w]);
//	    	System.out.println("genwC[w]: " + genwC[w]);
	    	
			//Draw U strip shape ADC value with gaussian fit
			namedir = String.format("crossStripHisto%02d", iteration - 1);
			name = String.format("adu_sig");
			name2 = String.format("ProjHistU%02d_W%02d", u + 1, w + 1);
		    h1  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, 0, 61, w, w);
		    namedir = String.format("GaussFit%02d", iteration-1);
			name = String.format("gaussU%02d_%02d", u + 1, w + 1);
			gaus  = (F1D)getDir().getDirectory(namedir).getObject(name);
	        canvas.cd(0);
	        //canvas.getPad().setStatBoxFont("Helvetica", 12);
	        canvas.draw(h1);
	        canvas.draw(gaus,"same");
	        
	        
	        //Draw W strip shape ADC value with gaussian fit
	        namedir = String.format("crossStripHisto%02d", iteration - 1);
			name = String.format("adw_sig");
			name2 = String.format("ProjHistW%02d_U%02d", w + 1, u + 1);
		    h1  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, 0, 61, w, w);
		    namedir = String.format("GaussFit%02d", iteration-1);
			name = String.format("gaussW%02d_%02d", w + 1, u + 1);
			gaus  = (F1D)getDir().getDirectory(namedir).getObject(name);
	        canvas.cd(1);
	        canvas.draw(h1);
	        canvas.draw(gaus,"same");
	        
	        
	        //Draw U attenuation with exponential
	        namedir = String.format("GraphE%02d", iteration-1);
			name = String.format("graphU_%02d", u + 1);
		    g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
		    g1.setMarkerSize(2);
		    namedir = String.format("ExpoFit%02d", iteration-1);
			name = String.format("expU_%02d", u + 1);
		    exp  = (F1D)getDir().getDirectory(namedir).getObject(name);
		    canvas.cd(2);
		    canvas.draw(g1);
		    //canvas.getPad().setAxisRange(0.0, umaxX[u] + 20.0, 0.0, 120.0);
		    canvas.draw(exp,"same");
		    
		    genexp1 = new F1D("exp+p0",0.0,umaxX[u]);
		    genexp1.setParameter(0,genuA[u]);// * (uA[u]+uC[u] + wA[w]+wC[w])/200.0);
		    genexp1.setParameter(1,genuB[u]);
		    genexp1.setParameter(2,genuC[u]);// * (uA[u]+uC[u] + wA[w]+wC[w])/200.0);
		    genexp1.setLineColor(2);
		    genexp1.setLineStyle(2);
		    canvas.draw(genexp1,"same");
	        
		    
		    //Draw W attenuation with exponential
	        namedir = String.format("GraphE%02d", iteration-1);
			name = String.format("graphW_%02d", w + 1);
		    g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
		    g1.setMarkerSize(2);
		    namedir = String.format("ExpoFit%02d", iteration-1);
			name = String.format("expW_%02d", w + 1);
		    exp  = (F1D)getDir().getDirectory(namedir).getObject(name);
	        canvas.cd(3);
	        canvas.draw(g1);
	        //canvas.getPad().setAxisRange(0.0, wmaxX[w] + 20.0, 0.0, 120.0);
	        canvas.draw(exp,"same");
	        
	        genexp = new F1D("exp+p0",0.0,wmaxX[w]);
		    genexp.setParameter(0,genwA[w]);// * (uA[u]+uC[u] + wA[w]+wC[w])/200.0);
		    genexp.setParameter(1,genwB[w]);
		    genexp.setParameter(2,genwC[w]);// * (uA[u]+uC[u] + wA[w]+wC[w])/200.0);
		    genexp.setLineColor(2);
		    genexp.setLineStyle(2);
		    canvas.draw(genexp,"same");
	          
        }
        if(desc.getLayer() == 4) //VU
        {
	        canvas.divide(2,1);
	        uvwnum = (int)desc.getComponent();
	    	u = (int)(uvwnum/100.0);
	    	uvwnum -= u*100;
	    	v = uvwnum;
	    	w = 61;
	    	
//	    	//Draw U strip shape ADC value with gaussian fit
//			namedir = String.format("Projection%02d", iteration);
//			name = String.format("ProjHistU%02d_W%02d", u + 1, w + 1);
//		    h1  = (MyH1D)getDir().getDirectory(namedir).getObject(name);
//		    namedir = String.format("GaussFit%02d", iteration);
//			name = String.format("gaussU%02d_%02d", u + 1, w + 1);
//			gaus  = (F1D)getDir().getDirectory(namedir).getObject(name);
//	        canvas.cd(0);
//	        canvas.draw(h1);
//	        canvas.draw(gaus,"same");
	        
	        
	        //Draw V strip shape ADC value with gaussian fit
	    	namedir = String.format("crossStripHisto%02d", iteration - 1);
			name = String.format("adv_sig");
			name2 = String.format("ProjHistV%02d_U%02d", v + 1, u + 1);
		    h1  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, v, v, 0, 61);
		    namedir = String.format("GaussFit%02d", iteration-1);
			name = String.format("gaussV%02d_%02d", v + 1, u + 1);
			gaus  = (F1D)getDir().getDirectory(namedir).getObject(name);
	        canvas.cd(0);
	        canvas.draw(h1);
	        canvas.draw(gaus,"same");
	        
	        
//	        //Draw U attenuation with exponential
//	        namedir = String.format("GraphE%02d", iteration);
//			name = String.format("graphU_%02d", u + 1);
//		    g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
//		    namedir = String.format("ExpoFit%02d", iteration);
//			name = String.format("expU_%02d", u + 1);
//		    exp  = (F1D)getDir().getDirectory(namedir).getObject(name);
//		    canvas.cd(2);
//		    canvas.draw(g1);
//		    canvas.draw(exp,"same");
	        
		    
		    //Draw V attenuation with exponential
	        namedir = String.format("GraphE%02d", iteration-1);
			name = String.format("graphV_%02d", v + 1);
		    g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
		    g1.setMarkerSize(2);
		    namedir = String.format("ExpoFit%02d", iteration-1);
			name = String.format("expV_%02d", v + 1);
		    exp  = (F1D)getDir().getDirectory(namedir).getObject(name);
	        canvas.cd(1);
	        canvas.draw(g1);
	        //canvas.getPad().setAxisRange(0.0, vmaxX[v] + 20.0, 0.0, 120.0);
	        canvas.draw(exp,"same");
	        
	        genexp = new F1D("exp+p0",0.0,vmaxX[v]);
		    genexp.setParameter(0,genvA[v]);// * (vA[v]+vC[v])/100.0);
		    genexp.setParameter(1,genvB[v]);
		    genexp.setParameter(2,genvC[v]);// * (vA[v]+vC[v])/100.0);
		    genexp.setLineColor(2);
		    genexp.setLineStyle(2);
		    canvas.draw(genexp,"same");
		    
	        
        }
        if(desc.getLayer() == 5) //WU
        {
        	//this.canvas = new EmbeddedCanvas();
        	//this.canvas.update();
	        canvas.divide(2,2);
	        uvwnum = (int)desc.getComponent();
	    	u = (int)(uvwnum/100.0);
	    	uvwnum -= u*100;
	    	w = uvwnum;
	    	v = 61;
	    	
	    	
//			//Draw U strip shape ADC value with gaussian fit
//			namedir = String.format("Projection%02d", iteration);
//			name = String.format("ProjHistU%02d_W%02d", u + 1, w + 1);
//		    h1  = (MyH1D)getDir().getDirectory(namedir).getObject(name);
//		    namedir = String.format("GaussFit%02d", iteration);
//			name = String.format("gaussU%02d_%02d", u + 1, w + 1);
//			gaus  = (F1D)getDir().getDirectory(namedir).getObject(name);
//	        canvas.cd(0);
//	        canvas.draw(h1);
//	        canvas.draw(gaus,"same");
	    	
	    	hsum1 = new MyH1D("uA + uC",40, 90.0, 500.0);
	        for(int i = 0; i < 68; ++i)
	        {
	        	hsum1.fill(ugain[i]);
	        }
	        hsum2 = new MyH1D("ugainselect",40, 90.0, 500.0);
	        hsum2.fill(ugain[u]);
	        hsum2.setLineColor(2); //getAttributes().getProperties().setProperty("line-color", "2");
	        canvas.cd(0);
	        canvas.draw(hsum1);
	        canvas.draw(hsum2,"same");
	        
	        
	        //Draw W strip shape ADC value with gaussian fit
	        namedir = String.format("crossStripHisto%02d", iteration - 1);
			name = String.format("adw_sig");
			name2 = String.format("ProjHistW%02d_U%02d", w + 1, u + 1);
		    h1  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, u, u, 0, 61, w, w);
		    namedir = String.format("GaussFit%02d", iteration-1);
			name = String.format("gaussW%02d_%02d", w + 1, u + 1);
			gaus  = (F1D)getDir().getDirectory(namedir).getObject(name);
	        canvas.cd(1);
	        canvas.draw(h1);
	        canvas.draw(gaus,"same");
	        
	        
//	        //Draw U attenuation with exponential
//	        namedir = String.format("GraphE%02d", iteration);
//			name = String.format("graphU_%02d", u + 1);
//		    g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
//		    namedir = String.format("ExpoFit%02d", iteration);
//			name = String.format("expU_%02d", u + 1);
//		    exp  = (F1D)getDir().getDirectory(namedir).getObject(name);
//		    canvas.cd(2);
//		    canvas.draw(g1);
//		    canvas.setAxisRange(0.0, umaxX[u] + 5.0, 0.0, 120.0);
//		    canvas.draw(exp,"same");
	        
	        
	    	hsum1 = new MyH1D("wA + wC",40, 90.0, 500.0);
	        for(int i = 0; i < 62; ++i)
	        {
	        	hsum1.fill(wgain[i]);
	        }
	        hsum2 = new MyH1D("wgainselect",40, 90.0, 500.0);
	        hsum2.fill(wgain[w]);
	        hsum2.setLineColor(2);
	        canvas.cd(2);
	        canvas.draw(hsum1);
	        canvas.draw(hsum2, "same");
	        
		    
		    //Draw W attenuation with exponential
	        namedir = String.format("GraphE%02d", iteration-1);
			name = String.format("graphW_%02d", w + 1);
		    g1  = (MyGraphErrors)getDir().getDirectory(namedir).getObject(name);
		    g1.setMarkerSize(2);
		    namedir = String.format("ExpoFit%02d", iteration-1);
			name = String.format("expW_%02d", w + 1);
		    exp  = (F1D)getDir().getDirectory(namedir).getObject(name);
	        canvas.cd(3);
	        canvas.draw(g1);
	        //canvas.getPad().setAxisRange(0.0, wmaxX[w] + 20.0, 0.0, 120.0);
	        canvas.draw(exp,"same");
	        
	        genexp = new F1D("exp+p0",0.0,wmaxX[w]);
		    genexp.setParameter(0,genwA[w]);// * (wA[w]+wC[w])/100.0);
		    genexp.setParameter(1,genwB[w]);
		    genexp.setParameter(2,genwC[w]);// * (wA[w]+wC[w])/100.0);
		    genexp.setLineColor(2);
		    genexp.setLineStyle(2);
		    canvas.draw(genexp,"same");
		    
        }
	        
    }
    
    /**
     * Each redraw of the canvas passes detector shape object to this routine
     * and user can change the color of specific component depending
     * on occupancy or some other criteria.
     * @param shape 
     */
    public void update(DetectorShape2D shape) {
        int sector = shape.getDescriptor().getSector();
        int paddle = shape.getDescriptor().getComponent();
        int layer = shape.getDescriptor().getLayer();
        //shape.setColor(200, 200, 200);
        
        int nent = nProcessed;
        Color col = palette.getColor3D(nent, nProcessed, true);
        shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
        /*
        if(this.tdcH.hasEntry(sector, 2,paddle)){
            int nent = this.tdcH.get(sector, 2,paddle).getEntries();
            Color col = palette.getColor3D(nent, nProcessed, true);
            //int colorRed = 240;
            //if(nProcessed!=0){
             //   colorRed = (255*nent)/(nProcessed);
            //}
            shape.setColor(col.getRed(),col.getGreen(),col.getBlue());
        }
        */
    }
    
   /*public void readStream() throws IOException {   
    	String dataFile = "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/evioFiles/pcal_4294.dat";
        DataInputStream in=null;
        try {
            in = new DataInputStream(new
                    BufferedInputStream(new FileInputStream(dataFile)));
            try {
                while (true) {
                     int npc = in.readInt();
                     for (int i=0; i<npc ; i++) {
                         int ev = in.readInt();
                       byte lay = in.readByte();
                       byte str = in.readByte();
                        int adc = in.readShort();
                       System.out.println("npc,ev,lay,str,adc="+npc+" "+ev+" "+lay+" "+str+" "+adc);
                     }
                }
            } catch (EOFException e) {
            } 
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        finally {
            in.close();
        }
        
}*/
    
    @Override
    public void processEvent(DataEvent de) 
    {
        EvioDataEvent event = (EvioDataEvent) de;
        int nh[][]         = new int[6][9];
		int strr[][][]     = new int[6][9][68]; 
		int adcr[][][]     = new int[6][9][68];
		float tdcr[][][]   = new float[6][9][68];
		int rs[]           = new int[9];
		int ad[]           = new int[9];
		float td[]         = new float[9];
		boolean good_lay[] = new boolean[9]; 
		boolean good_uv[]  = new boolean[3];
		boolean good_uw[]  = new boolean[3];
		boolean good_vw[]  = new boolean[3];
		boolean good_uvw[] = new boolean[3];		
		boolean good_uwt[] = new boolean[3];
		boolean good_vwt[] = new boolean[3];
		boolean good_wut[] = new boolean[3];
		boolean good_uwtt[]= new boolean[3];
		boolean good_vwtt[]= new boolean[3];
		boolean good_wutt[]= new boolean[3];
		int rscutuw[]      = {60,35,35};
		int rscutvw[]      = {67,35,35};
		int rscutwu[]      = {67,35,35};
		int rsw[]          = {0,1,1};
		int adcutuw[]      = {70,5,5};
		int adcutvw[]      = {70,5,5};
		int adcutwu[]      = {70,5,5};

		//int tid            = 100000;
		//int cid            = 10000;
		int thr            = 1; //threshold count
		int iis            = CurrentSector;	//Sector 2 hardwired for now
		
		float uvw=0;
		
		MyH1D hDalitz, hDalitzMCut;
		MyH2D HnumhitsUV, HnumhitsUW, HnumhitsVW;
		String name;
		String namedir;
		MyH1D pixelfilling;
		MyH4D pixelfilling4;
		
		for (int is=0 ; is<6 ; is++) //initialization
		{
			for (int il=0 ; il<9 ; il++) 
			{
				nh[is][il] = 0;
				for (int ip=0 ; ip<68 ; ip++) 
				{
					strr[is][il][ip] = 0;
					adcr[is][il][ip] = 0;
					tdcr[is][il][ip] = 0;
				}
			}
		}
		
		//check to see if MC entries
		double mc_t=0.0;
		if(event.hasBank("PCAL::true")==true)
		{
			//System.out.println("In MC loop");
			EvioDataBank bank = (EvioDataBank) event.getBank("PCAL::true");
			int nrows = bank.rows();
			for(int i=0; i < nrows; i++)
			{
				mc_t = bank.getDouble("avgT",i);
			}	
		}
		
		
		
		
		if(event.hasBank("PCAL::dgtz")==true)
		{
			int ic=0;	// ic=0,1,2 -> PCAL,ECinner,ECouter
			uvw=0;
			float tdcmax=100000;
            EvioDataBank bank = (EvioDataBank) event.getBank("PCAL::dgtz");
            
            for(int i = 0; i < bank.rows(); i++)
            {
            	float tdc = (float)bank.getInt("TDC",i)-(float)(mc_t)*1000;
            	if (tdc<tdcmax) tdcmax=tdc;
            }
            
            for(int i = 0; i < bank.rows(); i++)
            {
            	int is  = bank.getInt("sector",i);
            	int ip  = bank.getInt("strip",i);
            	int il  = bank.getInt("view",i);
            	int adc = bank.getInt("ADC",i);
            	//adc = adc * 100/650 ;
            	float tdc =(float)(bank.getInt("TDC",i));
            	tdc=((tdc-(float)mc_t*1000)-tdcmax+1340000)/1000;
            	
            	//System.out.println(adc + "   "  + is);
            	
                if(is==iis)//whatever sector mentioned
                {
            	   if (adc>thr) 
            	   {
            	     nh[is-1][il-1]++;
            	     int inh = nh[is-1][il-1];
            	     adcr[is-1][il-1][inh-1] = adc;
            	     //tdcr[is-1][il-1][inh-1] = tdc;
            	     strr[is-1][il-1][inh-1] = ip;
            	     uvw=uvw+pcal.uvw_dalitz(ic,ip,il);
            	   }
            	   namedir = String.format("dalitz%02d", iteration);
            	   hDalitz = (MyH1D) getDir().getDirectory(namedir).getObject("Dalitz Condition");
                   hDalitz.fill(uvw);
                }
            }
            

         }
		
        
        // Logic: Limit multiplicity to 1 hit per view
        for (int il=0 ; il<9 ; il++)
        {
        	good_lay[il]=nh[iis-1][il]==1;
        	if (good_lay[il]) 
        	{
        		rs[il]=strr[iis-1][il][0];
        		ad[il]=adcr[iis-1][il][0];
        		td[il]=tdcr[iis-1][il][0];
        	}
        }
        
        // Logic: Good two-view and three-view multiplicity (m2,m3 cut)
        for (int ic=0 ; ic<3; ic++)
        {
        	good_uv[ic]   = good_lay[0+ic*3]&good_lay[1+ic*3];
        	good_uw[ic]   = good_lay[0+ic*3]&good_lay[2+ic*3];
        	good_vw[ic]   = good_lay[1+ic*3]&good_lay[2+ic*3];
        	good_uvw[ic]  = good_uv[ic]&good_lay[2+ic*3];
        	
        	good_uwt[ic]  =  good_uw[ic]&rs[2+ic*3]==rscutuw[ic];
        	good_vwt[ic]  =  good_uv[ic]&rs[0+ic*3]==rscutvw[ic];
        	good_wut[ic]  =  good_uw[ic]&rs[rsw[ic]+ic*3]==rscutwu[ic];
        	good_uwtt[ic] = good_uwt[ic]&ad[2+ic*3]>adcutuw[ic];
        	good_vwtt[ic] = good_vwt[ic]&ad[0+ic*3]>adcutvw[ic];
        	good_wutt[ic] = good_wut[ic]&ad[rsw[ic]+ic*3]>adcutwu[ic];        	
        }  
		
        for (int ic=0 ; ic<3 ; ic++)//ic = {PCAL, EC inner, EC outer}
        {
        	if (good_uvw[ic])//multiplicity
        	{
        		if(ic == 0)//PCAL
        		{
        			namedir = String.format("dalitz%02d", iteration);
        			hDalitzMCut = (MyH1D) getDir().getDirectory(namedir).getObject("Dalitz Multiplicity Cut");
        			hDalitzMCut.fill(uvw);
        			HnumhitsUV = (MyH2D) getDir().getDirectory(namedir).getObject("numHitsUV");
        			HnumhitsUV.fill(rs[0],rs[1]);
        			HnumhitsUW = (MyH2D) getDir().getDirectory(namedir).getObject("numHitsUW");
        			HnumhitsUW.fill(rs[0],rs[2]);
        			HnumhitsVW = (MyH2D) getDir().getDirectory(namedir).getObject("numHitsVW");
        			HnumhitsVW.fill(rs[1],rs[2]);
        			
        			//System.out.println("adc U " + rs[0] + "      adc V " + rs[1] + "      adc W " + rs[2]);

        			//adc on u strip
        			namedir = String.format("crossStripHisto%02d", iteration);
            		name = String.format("adu_sig");
            		pixelfilling4 = (MyH4D)getDir().getDirectory(namedir).getObject(name);
            		//if(iteration != 0)
            		//{
	            		if(Math.abs(ad[1] - (genvA[rs[1]-1]*Math.exp(genvB[rs[1]-1] * vdpixel[rs[0]-1][rs[1]-1][rs[2]-1]) + genvC[rs[1]-1])) < 150.0
	            		&& Math.abs(ad[2] - (genwA[rs[2]-1]*Math.exp(genwB[rs[2]-1] * wdpixel[rs[0]-1][rs[1]-1][rs[2]-1]) + genwC[rs[2]-1])) < 150.0)
	            		//if(Math.abs(ad[0] - (Umean[rs[0]-1][rs[2]-1])) < Usigma[rs[0]-1][rs[2]-1]
	            		//&& Math.abs(ad[2] - (genwA[rs[2]-1]*Math.exp(genwB[rs[2]-1] * wdpixel[rs[0]-1][rs[1]-1][rs[2]-1]) + genwC[rs[2]-1])) < 50.0)
	            			pixelfilling4.fill(ad[0], rs[0], rs[1], rs[2]);
            		//}
            		//else
            		//{
            		//	pixelfilling4.fill(ad[0], rs[0], rs[1], rs[2]);
            		//}
            			
            		//adc on v strip
            		namedir = String.format("crossStripHisto%02d", iteration);
            		name = String.format("adv_sig");
            		pixelfilling4 = (MyH4D)getDir().getDirectory(namedir).getObject(name);
            		//if(iteration != 0)
            		//{
	            		if(Math.abs(ad[0] - (genuA[rs[0]-1]*Math.exp(genuB[rs[0]-1] * udpixel[rs[0]-1][rs[1]-1][rs[2]-1]) + genuC[rs[0]-1])) < 150.0
	            		&& Math.abs(ad[2] - (genwA[rs[2]-1]*Math.exp(genwB[rs[2]-1] * wdpixel[rs[0]-1][rs[1]-1][rs[2]-1]) + genwC[rs[2]-1])) < 150.0)
	            			pixelfilling4.fill(ad[1], rs[0], rs[1], rs[2]);
            		//}
            		//else
            		//{
            		//	pixelfilling4.fill(ad[1], rs[0], rs[1], rs[2]);
            		//}
            		
            		
            		//adc on w strip
            		namedir = String.format("crossStripHisto%02d", iteration);
            		name = String.format("adw_sig");
            		pixelfilling4 = (MyH4D)getDir().getDirectory(namedir).getObject(name);
            		//if(iteration != 0)
            		//{
	            		if(Math.abs(ad[1] - (genvA[rs[1]-1]*Math.exp(genvB[rs[1]-1] * vdpixel[rs[0]-1][rs[1]-1][rs[2]-1]) + genvC[rs[1]-1])) < 150.0
	            		&& Math.abs(ad[0] - (genuA[rs[0]-1]*Math.exp(genuB[rs[0]-1] * udpixel[rs[0]-1][rs[1]-1][rs[2]-1]) + genuC[rs[0]-1])) < 150.0)
	            			pixelfilling4.fill(ad[2], rs[0], rs[1], rs[2]);
            		//}
            		//else
            		//{
            		//	pixelfilling4.fill(ad[2], rs[0], rs[1], rs[2]);
            		//}
            		
            			
            		//check if pixel is valid with hitmatrix
            		//fill pixel histograms initialized by Nickinit()
            		if(hit[rs[0] - 1][rs[1] - 1][rs[2] - 1] == 1 && iteration == 0)
            		{
            			double ucor = ((ad[0] - uC[rs[0]-1])/Math.exp(uB[rs[0]-1] * udpixel[rs[0]-1][rs[1]-1][rs[2]-1])) + uC[rs[0]-1];
            			double vcor = ((ad[1] - vC[rs[1]-1])/Math.exp(vB[rs[1]-1] * vdpixel[rs[0]-1][rs[1]-1][rs[2]-1])) + vC[rs[1]-1];
            			double wcor = ((ad[2] - wC[rs[2]-1])/Math.exp(wB[rs[2]-1] * wdpixel[rs[0]-1][rs[1]-1][rs[2]-1])) + wC[rs[2]-1];
            			
            			namedir = String.format("pixelsignal%02d", iteration);
            			name = String.format("Uen_%02d_%02d_%02d", rs[0], rs[1], rs[2]);
            			pixelfilling = (MyH1D)getDir().getDirectory(namedir).getObject(name);
            			pixelfilling.fill(ucor);
            			
            			namedir = String.format("pixelsignal%02d", iteration);
            			name = String.format("Ven_%02d_%02d_%02d", rs[0], rs[1], rs[2]);
            			pixelfilling = (MyH1D)getDir().getDirectory(namedir).getObject(name);
            			pixelfilling.fill(vcor);
            			
            			namedir = String.format("pixelsignal%02d", iteration);
            			name = String.format("Wen_%02d_%02d_%02d", rs[0], rs[1], rs[2]);
            			pixelfilling = (MyH1D)getDir().getDirectory(namedir).getObject(name);
            			pixelfilling.fill(wcor);
            			
            			namedir = String.format("pixelsignal%02d", iteration);
            			name = String.format("toten_%02d_%02d_%02d", rs[0], rs[1], rs[2]);
            			pixelfilling = (MyH1D)getDir().getDirectory(namedir).getObject(name);
            			pixelfilling.fill((ucor + vcor + wcor)/3.0);

        			}
        		}
        	}
        }
        
    }

    
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().compareTo("Process")==0){
            DetectorEventProcessorDialog dialog = new DetectorEventProcessorDialog(this);
        }
    }
  
    
    public void process(){
    	   ProgressPrintout printout = new ProgressPrintout("Calibration");
    	   printout.setInterval(1.0);
    	   
    	   for(int file = 0; file < 8; ++file)
    	   {
	    	   EvioSource  reader = new EvioSource();
	    	   //reader.open(this.inputFileName[file]);
	    	   reader.open(this.inputFileName[file]);
	    	   int icounter = 0;
	    	   while(reader.hasEvent()){// && icounter < 500000){
	    	       icounter++;
	    	       EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
	    	       try {
	    	           processEvent(event);
	    	       } catch (Exception e){
	    	           System.err.println("[PCAL Calibration] ----> error in event " + icounter);
	    	           e.printStackTrace();
	    	       }
	    	       
	    	       printout.setAsInteger("nevents", icounter);
	    	       printout.updateStatus();
	    	   }
	    	   reader.close();
    	   }
    	   //this.analyze();
    	}
    
    
    public void Nickanalyze() 
	{
		double centroids[] = new double[20000];
		double x[] = new double[20000];
		double ex[] = new double[20000];
		double ey[] = new double[20000];
		int counter = 0;
		//RootCanvas canvas = new RootCanvas();
		
		MyH1D tempsignal;
		F1D myfunc2;
		MyGraphErrors attengraph;
		String name, name2;
		String namedir;
		String gaussfName;
		
		//fit function
		F1D gausFit3strip;
		
		namedir = String.format("attendir%02d", iteration);
		TDirectory fitdir = new TDirectory(namedir); //creates directory
		
		String gaussfNameFormat[] = {"gaussU%02d_%02d_%02d", "gaussV%02d_%02d_%02d", "gaussW%02d_%02d_%02d"};
		
		
		/////////////////// find u strip attenuation /////////////////////////////
		counter = 0;
		for(int ustrip = 0; ustrip < 68; ++ustrip)
		{
			counter = 0;
			for(int vstrip = 0; vstrip < 62; ++vstrip)
			{
				for(int wstrip = 0; wstrip < 62; ++wstrip)
				{
					if(hit[ustrip][vstrip][wstrip] == 1)// && udpixel[ustrip][vstrip][wstrip] > 0.0 && vdpixel[ustrip][vstrip][wstrip] > 0.0  && wdpixel[ustrip][vstrip][wstrip] > 0.0
					{
						namedir = String.format("crossStripHisto%02d", iteration);
						name = String.format("adu_sig");
						name2 = String.format("adu_%02d_%02d_%02d", ustrip + 1, vstrip + 1, wstrip + 1);
						tempsignal  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, ustrip, ustrip, vstrip, vstrip, wstrip, wstrip);

						//create gaussian function and fit
						gausFit3strip = new F1D("gaus",0.0,800.0);
						gaussfName = String.format(gaussfNameFormat[0], ustrip+1 , vstrip+1, wstrip+1);
						gausFit3strip.setName(gaussfName);
						//gausFit3strip.setParameter(0, ProjHadc.getBinContent(ProjHadc.getMaximumBin()));
						//gausFit3strip.setParLimits(0, 0.0, 500.0);
						//gausFit3strip.setParameter(1, ProjHadc.getMean());
						//gausFit3strip.setParLimits(1, 0.0, 150.0);
						//gausFit3strip.setParLimits(1, 0.0, 800.0);
						//gausFit3strip.setParameter(2, ProjHadc.getRMS());
						//gausFit3strip.setParLimits(2, 0.0, 200.0);
						
						//centroids[counter] = myfunc.getParameter(1);
						if(tempsignal.getEntries() > 10)
						{
							Hpixcounts.fill(tempsignal.getEntries());
							
							tempsignal.fit(gausFit3strip, "REQ");
							centroids[counter] = gausFit3strip.getParameter(1);
							ey[counter] = (float)gausFit3strip.getParameter(2)/Math.sqrt(tempsignal.getEntries());
							
							x[counter] = udpixel[ustrip][vstrip][wstrip];
							ex[counter] = 0.0;
							++counter;
						}
						/*else if (tempsignal.getMean() > 1)
						{
							//Hpixcounts.fill(tempsignal.getEntries());
							
							centroids[counter] = tempsignal.getMean();
							ey[counter] = tempsignal.getRMS()/tempsignal.getEntries(); 
							
							x[counter] = udpixel[ustrip][vstrip][wstrip];
							ex[counter] = 0.0;
							++counter;
							
						}*/
						
					
					}
				}
			}
			name = String.format("attu_%02d", ustrip + 1);
			attengraph = graphn(name,counter,x,centroids,ex,ey);
		
			//create function and fit
			//myfunc2 = new F1D("exp+p0",0.0,umaxX[ustrip]); //"mycustomfunc",
			myfunc2 = new F1D("exp+p0",0.0,500.0); //"mycustomfunc",
			name = String.format("attufit_%02d", ustrip + 1);
			myfunc2.setName(name);
			//if(uA[ustrip] < 800.0)
			//	myfunc2.parameter(0).setValue(uA[ustrip]);
			//else
				myfunc2.parameter(0).setValue(100.0);
			myfunc2.setParLimits(0, 0.0, 800.0);
			myfunc2.parameter(1).setValue(uB[ustrip]);
			//myfunc2.setParLimits(1, -10.0, 10.0);
			//if(uC[ustrip] < 105.0)
			//	myfunc2.parameter(2).setValue(uC[ustrip]);
			//else
				myfunc2.parameter(2).setValue(15.0);
			//myfunc2.setParLimits(2, 0.0, 800.0);
			attengraph.fit(myfunc2);
			
			fitdir.add(myfunc2);
			fitdir.add(attengraph);
		}
		
		/////////////////// find v strip attenuation /////////////////////////////
		for(int vstrip = 0; vstrip < 62; ++vstrip)
		{
			counter = 0;
			for(int ustrip = 0; ustrip < 68; ++ustrip)
			{
				for(int wstrip = 0; wstrip < 62; ++wstrip)
				{
					if(hit[ustrip][vstrip][wstrip] == 1)// && udpixel[ustrip][vstrip][wstrip] > 0.0 && vdpixel[ustrip][vstrip][wstrip] > 0.0  && wdpixel[ustrip][vstrip][wstrip] > 0.0
					{
						namedir = String.format("crossStripHisto%02d", iteration);
						name = String.format("adv_sig");
						name2 = String.format("adv_%02d_%02d_%02d", ustrip + 1, vstrip + 1, wstrip + 1);
						tempsignal  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, ustrip, ustrip, vstrip, vstrip, wstrip, wstrip);

						//centroids[counter] = myfunc.getParameter(1);
						if(tempsignal.getMean() > 1)
						{
							centroids[counter] = tempsignal.getMean();
							ey[counter] = tempsignal.getRMS()/tempsignal.getEntries(); 
							
							x[counter] = vdpixel[ustrip][vstrip][wstrip];
							ex[counter] = 1.0;
							++counter;
						}
						
					
					}
				}
			}
			name = String.format("attv_%02d", vstrip + 1);
			attengraph = graphn(name,counter,x,centroids,ex,ey);
		
			//create function and fit
			myfunc2 = new F1D("exp",0.0,800.0); //"mycustomfunc",
			myfunc2.parameter(0).setValue(600.0);
			//myfunc2.setParLimits(0, 0.0, 200.0);
			myfunc2.parameter(1).setValue(-0.002659574468);
			//myfunc2.setParLimits(1, 0.0, 105.0);
			//attengraph.fit(myfunc2);
			
			fitdir.add(attengraph);
		}
		
		/////////////////// find w strip attenuation /////////////////////////////
		for(int wstrip = 0; wstrip < 62; ++wstrip)
		{
			counter = 0;
			for(int vstrip = 0; vstrip < 62; ++vstrip)
			{
				for(int ustrip = 0; ustrip < 68; ++ustrip)
				{
					if(hit[ustrip][vstrip][wstrip] == 1)// && udpixel[ustrip][vstrip][wstrip] > 0.0 && vdpixel[ustrip][vstrip][wstrip] > 0.0  && wdpixel[ustrip][vstrip][wstrip] > 0.0
					{
						namedir = String.format("crossStripHisto%02d", iteration);
						name = String.format("adw_sig");
						name2 = String.format("adw_%02d_%02d_%02d", ustrip + 1, vstrip + 1, wstrip + 1);
						tempsignal  = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(name)).projectionX(name2, ustrip, ustrip, vstrip, vstrip, wstrip, wstrip);

						//centroids[counter] = myfunc.getParameter(1);
						if(tempsignal.getMean() > 1)
						{
							centroids[counter] = tempsignal.getMean();
							ey[counter] = tempsignal.getRMS()/tempsignal.getEntries(); 
							
							x[counter] = wdpixel[ustrip][vstrip][wstrip];
							ex[counter] = 1.0;
							++counter;
						}
					}
				}
			}
			name = String.format("attw_%02d", wstrip + 1);
			attengraph = graphn(name,counter,x,centroids,ex,ey);
		
			//create function and fit
			myfunc2 = new F1D("exp",0.0,800.0); //"mycustomfunc",
			myfunc2.parameter(0).setValue(600.0);
			//myfunc2.setParLimits(0, 0.0, 200.0);
			myfunc2.parameter(1).setValue(-0.002659574468);
			//myfunc2.setParLimits(1, 0.0, 105.0);
			//attengraph.fit(myfunc2);
			
			fitdir.add(attengraph);
		}
		
		getDir().addDirectory(fitdir);

		
	}
	
    	 //MyGraphErrors constructor
  	
    public MyGraphErrors graphn(String name, int numberpoints, double x[], double y[], double xe[], double ye[])
    {
          double a[] = new double[numberpoints];
          double b[] = new double[numberpoints];
          double ae[] = new double[numberpoints];
          double be[] = new double[numberpoints];
          
          for(int i = 0; i < numberpoints; ++i)
          {
              a[i] = x[i];
              ae[i] = xe[i];
              b[i] = y[i];
              be[i] = ye[i];            
          }
          
          MyGraphErrors mygraph = new MyGraphErrors(a,b,ae,be);
          mygraph.setName(name);
          mygraph.setTitle(name);
          
          return mygraph;
    }
  	
    
    public F1D fitexp(double x[], int counter, int strip, String functionName, int uvw)
    {
    	  	double minx, maxx;
			if(counter < 3) //no valid points
			{
				minx = 0.0;
				if(uvw == 0)
				{
					maxx = umaxX[strip];
				}
				else if(uvw == 1)
				{
					maxx = vmaxX[strip];
				}
				else
				{
					maxx = wmaxX[strip];
				}
			}
			else if(counter < 4) //3 points, 1 non-excluded points
			{
				if(x[0] > x[counter-1])
				{
					minx = (x[counter-1] + x[counter-2] + x[counter-2])/3.0;
					maxx = (x[0] + x[1] + x[1])/3.0;
				}
				else
				{
					minx = (x[0] + x[1] + x[1])/3.0;
					maxx = (x[counter-1] + x[counter-2] + x[counter-2])/3.0;
				}
			}
			else if(counter < 5) //4 points, 2 non-excluded points
			{
				//means low number of points
				//probably short strip
				if(x[0] > x[counter-1])
				{
					minx = (x[counter-1] + x[counter-2] + x[counter-2])/3.0;
					maxx = (x[0] + x[1] + x[1])/3.0;
				}
				else
				{
					minx = (x[0] + x[1] + x[1])/3.0;
					maxx = (x[counter-1] + x[counter-2] + x[counter-2])/3.0;
				}
			}
			else
			{
				if(x[0] > x[counter-1])
				{
					minx = (x[counter-1] + x[counter-2] + x[counter-2])/3.0;
					maxx = (x[0] + x[1] + x[1])/3.0;
				}
				else
				{
					minx = (x[0] + x[1] + x[1])/3.0;
					maxx = (x[counter-1] + x[counter-2] + x[counter-2])/3.0;
				}
			}
			
		  //initialize function
			F1D fitatten; 
    	    if(counter < 3) //no valid points
    	    {
    	    	fitatten = new F1D("p0",minx,maxx);
    	    	//fitatten.fixParameter(1, 0.0); //straight line, not x dependent
    	    	//fitatten.fixParameter(2, 0.0);
			}
			else if(counter < 4) //3 points, 1 non-excluded points
			{
				fitatten = new F1D("p0",minx,maxx);
				//fitatten.fixParameter(1, 0.0); //straight line, not x dependent
    	    	//fitatten.fixParameter(2, 0.0);
			}
			else if(counter < 5) //4 points, 2 non-excluded points
			{
				fitatten = new F1D("exp",minx,maxx);
    	    	//fitatten.fixParameter(2, 0.0); //just exponential
			}
			else
			{
				fitatten = new F1D("exp+p0",minx,maxx);
				if(uvw == 0) //ustrip
		    	  {
		    	  	fitatten.parameter(0).setValue(genuA[strip]);
		    	  	fitatten.parameter(1).setValue(genuB[strip]);
		    	  	fitatten.parameter(2).setValue(genuC[strip]);
		    	  }
		    	  else if(uvw == 1) //vstrip
		    	  {
		    		fitatten.parameter(0).setValue(genvA[strip]);
		      	  	fitatten.parameter(1).setValue(genvB[strip]);
		      	  	fitatten.parameter(2).setValue(genvC[strip]);
		    	  }
		    	  else //wstrip
		    	  {
		    		fitatten.parameter(0).setValue(genwA[strip]);
		      	  	fitatten.parameter(1).setValue(genwB[strip]);
		      	  	fitatten.parameter(2).setValue(genwC[strip]);
		    	  }
		    	  
		    	  //make sure parameters are reasonable
		    	  if(fitatten.getParameter(0) > 800.0 || fitatten.getParameter(0) < 0.0)
		    		  fitatten.parameter(0).setValue(200.0);
		    	  if(fitatten.getParameter(1) > 5.0 || fitatten.getParameter(1) < -5.0)
		    		  fitatten.parameter(1).setValue(-0.005);
		    	  if(fitatten.getParameter(2) > 200.0 || fitatten.getParameter(2) < 0.0)
		    		  fitatten.parameter(2).setValue(0.0);
		    	  
		    	  //minimize the checking range
		    	  fitatten.setParLimits(0, 0.0, 800.0);
		    	  fitatten.setParLimits(1, -5.0, 5.0);
		    	  fitatten.setParLimits(2, 0.0, 500.0);
			}
    	  
    	  
    	  // set function name
    	  fitatten.setName(functionName);
    	 
    	  //pass back function with its parameters
    	  return fitatten;
    }

	
	public void init() 
	{
		String histname;
		String namedir;
	
		namedir = String.format("crossStripHisto%02d", iteration);
		TDirectory geometry = new TDirectory(namedir);
		namedir = String.format("dalitz%02d", iteration);
		TDirectory calADC = new TDirectory(namedir);
		
		calADC.add(new MyH1D("Dalitz Condition",500,0.,3.0));
		calADC.add(new MyH1D("Dalitz Multiplicity Cut",500,0.,3.0));
		calADC.add(new MyH2D("numHitsUV",68,0.5,68.5,62,0.5,62.5));
		calADC.add(new MyH2D("numHitsUW",68,0.5,68.5,62,0.5,62.5));
		calADC.add(new MyH2D("numHitsVW",62,0.5,62.5,62,0.5,62.5));
		
	
		histname = String.format("adu_sig");
		//geometry.add(new MyH4D(histname,100,0.0,300.0,68,0.5,68.5,62,0.5,62.5,62,0.5,62.5));
		geometry.add(new MyH4D(histname,80,0.0,800.0,68,0.5,68.5,62,0.5,62.5,62,0.5,62.5));
		
		histname = String.format("adv_sig");
		//geometry.add(new MyH4D(histname,100,0.0,300.0,68,0.5,68.5,62,0.5,62.5,62,0.5,62.5));
		geometry.add(new MyH4D(histname,80,0.0,800.0,68,0.5,68.5,62,0.5,62.5,62,0.5,62.5));

		histname = String.format("adw_sig");
		//geometry.add(new MyH4D(histname,100,0.0,300.0,68,0.5,68.5,62,0.5,62.5,62,0.5,62.5));
		geometry.add(new MyH4D(histname,80,0.0,800.0,68,0.5,68.5,62,0.5,62.5,62,0.5,62.5));
		
		//this directory is for attenuation 2D plots
		getDir().addDirectory(geometry);
		//this directory is for other interesting quanitities
		getDir().addDirectory(calADC);
		
		
		//pixel corrected energy
		int ustrip, vstrip, wstrip;
		//int counter = 0;
		String name;
		MyH1D sig;
		
		namedir = String.format("pixelsignal%02d", iteration);
		TDirectory pixelsignal = new TDirectory(namedir);
		
		for(ustrip = 0; ustrip < 68; ++ustrip)
		{
			for(vstrip = 0; vstrip < 62; ++vstrip)
			{
				for(wstrip = 0; wstrip < 62; ++wstrip)
				{
					if(hit[ustrip][vstrip][wstrip] == 1)
					{						
						name = String.format("Uen_%02d_%02d_%02d", ustrip + 1, vstrip + 1, wstrip + 1);
						//sig = new MyH1D(name,100,0.0,300.0);
						sig = new MyH1D(name,100,0.0,800.0);
						pixelsignal.add(sig);
						
						name = String.format("Ven_%02d_%02d_%02d", ustrip + 1, vstrip + 1, wstrip + 1);
						//sig = new MyH1D(name,100,0.0,300.0);
						sig = new MyH1D(name,100,0.0,800.0);
						pixelsignal.add(sig);
						
						name = String.format("Wen_%02d_%02d_%02d", ustrip + 1, vstrip + 1, wstrip + 1);
						//sig = new MyH1D(name,100,0.0,300.0);
						sig = new MyH1D(name,100,0.0,800.0);
						pixelsignal.add(sig);
						
						name = String.format("toten_%02d_%02d_%02d", ustrip + 1, vstrip + 1, wstrip + 1);
						//sig = new MyH1D(name,100,0.0,300.0);
						sig = new MyH1D(name,100,0.0,800.0);
						pixelsignal.add(sig);
						
						
					}
				}
			}
		}
		
		
		getDir().addDirectory(pixelsignal);
	}
    
	
	public void analyze() 
	{
		//create directories
		String namedir;
		//namedir = String.format("Projection%02d", iteration);
		//TDirectory projection = new TDirectory(namedir);
		namedir = String.format("GaussFit%02d", iteration);
		TDirectory gausFitDir = new TDirectory(namedir);
		namedir = String.format("ExpoFit%02d", iteration);
		TDirectory expofit = new TDirectory(namedir);
		namedir = String.format("GraphE%02d", iteration);
		TDirectory graph = new TDirectory(namedir);
		
		double centroids[] = new double[500];
		double centroidsErr[] = new double[500];
		
		double x[] = new double[500];
		double ex[] = new double[500];
		double ey[] = new double[500];
		double xTemp[]   = new double[500];
		double xTempEr[] = new double[500];
		double minx = 0.0;
		double maxx = 0.0;
		int counter = 0;
		
		//histograms
		MyH2D Hadc[] = new MyH2D[100];
		//projections
		MyH1D ProjHadc = new MyH1D();
		
		//fit function
		F1D expfit;
		F1D gausFit;//[] = new F1D[62*68];

		String histName;
		String projName;
		String graphName;
		//String plotName;
		String functionName;
		String gaussFuncName;

		
		PrintWriter writer = null;
	
		//U,V,W strip calibration
		//int count = 0;
		
		char stripLetter[] = {'u','v','w'};
		//char stripLetter2[] = {'w','u','u'};
		//String cstring1, cstring2;
		String histNameFormat[] = {"histU_%02d", "histV_%02d", "histW_%02d"};
		String projNameFormat[] = {"ProjHistU%02d_W%02d", "ProjHistV%02d_U%02d", "ProjHistW%02d_U%02d"};
		String gaussfuncNameFormat[] = {"gaussU%02d_%02d", "gaussV%02d_%02d", "gaussW%02d_%02d"};
		String graphNameFormat[] = {"graphU_%02d", "graphV_%02d", "graphW_%02d"};
		String functionNameFormat[] = {"expU_%02d", "expV_%02d", "expW_%02d"};
		String fileName[] = {"/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/UattenCoeff.dat", 
							 "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/VattenCoeff.dat", 
							 "/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/WattenCoeff.dat"};
		
		int stripMax[] = {68, 62, 62};//u, v, w
		int crossStripMax[] = {62, 68, 68};//w, u, u
		
		//create gaussian function and fit
		
		for(int il = 0; il < 3; ++il)
		{
			try 
			{
				writer = new PrintWriter(fileName[il]);
			} 
			catch (FileNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//TCanvas Ucan = new TCanvas("Ucan", "Ucan", 800, 600, 2, 2);
			for(int strip = 0; strip < stripMax[il]; ++strip)//calibrating u-strip
			{
				histName = String.format(histNameFormat[il], strip + 1);
				namedir = String.format("crossStripHisto%02d", iteration);
				//Hadc[strip] = (MyH2D)getDir().getDirectory(namedir).getObject(histName);
				counter = 0;
				for(int crossStrip = 0; crossStrip < crossStripMax[il]; crossStrip++)
				{
					projName = String.format(projNameFormat[il], strip+1, crossStrip + 1);
					
					if(stripLetter[il] == 'u')
					{
						histName = String.format("adu_sig");
						projName = String.format("adu_%02d_%02d", strip, crossStrip);
						ProjHadc = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(histName)).projectionX(projName, strip, strip, 0, 61, crossStrip, crossStrip);
					}
					else if(stripLetter[il] == 'v')
					{
						histName = String.format("adv_sig");
						projName = String.format("adv_%02d_%02d", strip, crossStrip);
						ProjHadc = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(histName)).projectionX(projName, crossStrip, crossStrip, strip, strip, 0, 61);
					}
					else if(stripLetter[il] == 'w')
					{
						histName = String.format("adw_sig");
						projName = String.format("adw_%02d_%02d", strip, crossStrip);
						ProjHadc = (MyH1D)((MyH4D)getDir().getDirectory(namedir).getObject(histName)).projectionX(projName, crossStrip, crossStrip, 0, 61, strip, strip);
					}
					else
					{
						System.err.println("Projections are not working right.");
						System.err.println(il + "    " + strip + "    " + crossStrip);
					}

					
					//ProjHadc[crossStrip] = Hadc[strip].sliceX(crossStrip);
					//ProjHadc[crossStrip].setName(projName);
								
					//create gaussian function and fit
					gausFit = new F1D("gaus",0.0,800.0);
					gaussFuncName = String.format(gaussfuncNameFormat[il], strip+1, crossStrip + 1);
					gausFit.setName(gaussFuncName);
					//gausFit.setParameter(0, ProjHadc.getBinContent(ProjHadc.getMaximumBin()));
					//gausFit.setParLimits(0, 0.0, 500.0);
					//gausFit.setParameter(1, ProjHadc.getMean());
					//gausFit.setParLimits(1, 0.0, 150.0);
					//gausFit.setParLimits(1, 0.0, 800.0);
					//gausFit.setParameter(2, ProjHadc.getRMS());
					//gausFit.setParLimits(2, 0.0, 200.0);
					
					
					//projection.add(ProjHadc[crossStrip]);
					
					//if(ProjHadc.getEntries() >= 10)
					//{
						//System.out.println(il + "    " + strip + "    " + crossStrip);
						//ProjHadc.fit(gausFit,"REQ");
						gausFitDir.add(gausFit);
						
						//centroids[counter] = gausFit.getParameter(1);
						//centroidsErr[counter] = (float)gausFit.getParameter(2)/Math.sqrt(ProjHadc.getEntries());
					//}
					//else
					//{
					
						centroids[counter] = ProjHadc.getMean();
						centroidsErr[counter] = (float)ProjHadc.getRMS()/Math.sqrt(ProjHadc.getEntries());
						
					//}
					gausFit = null;
					if(centroids[counter] >= 1.0)
					{
						xTemp[counter] = pcal.CalcDistinStrips(stripLetter[il], crossStrip+1)[0];//calcDistinStrips
						xTempEr[counter] = pcal.CalcDistinStrips(stripLetter[il], crossStrip+1)[1];//calcDistinStrips
						
						x[counter] = pcal.CalcDistance(stripLetter[il], xTemp[counter], xTempEr[counter])[0];
						ex[counter] = pcal.CalcDistance(stripLetter[il], xTemp[counter], xTempEr[counter])[1];
						//centroids[counter] = ProjHadc[crossStrip].getMean();
						if(stripLetter[il] == 'u' && x[counter] > umaxX[strip]) umaxX[strip] = x[counter] + 5;
						if(stripLetter[il] == 'v' && x[counter] > vmaxX[strip]) vmaxX[strip] = x[counter] + 5;
						if(stripLetter[il] == 'w' && x[counter] > wmaxX[strip]) wmaxX[strip] = x[counter] + 5;
						ey[counter] = 1.0;
						counter++;
					}
				}
				graphName = String.format(graphNameFormat[il], strip + 1);
				MyGraphErrors attengraph = graphn(graphName,counter,x,centroids,ex,centroidsErr);
				
				
				//set up function name
				functionName = String.format(functionNameFormat[il], strip + 1);
		
				//create and name function
				expfit = fitexp(x, counter, strip, functionName, il);
	
				//fit function
				attengraph.fit(expfit,"REQ");
				
				//add to directories
				graph.add(attengraph);
				expofit.add(expfit);

				//record parameters
				int j = strip+1;
				if(counter < 4)
				{
					writer.println(j  + "   " + x[0] + "   "
							+ expfit.getParameter(0) + "   " 
							+ 0.0 + "   " 
							+ 0.0);
				}
				else if(counter < 5)
				{
					writer.println(j  + "   " + x[0] + "   "
							+ expfit.getParameter(0) + "   " 
							+ expfit.getParameter(1) + "   " 
							+ 0.0);
				}
				else
				{
					writer.println(j  + "   " + x[0] + "   "
							+ expfit.getParameter(0) + "   " 
							+ expfit.getParameter(1) + "   " 
							+ expfit.getParameter(2));
				}
			}
			writer.close();
		}
		//getDir().addDirectory(projection);
		getDir().addDirectory(gausFitDir);
		getDir().addDirectory(expofit);
		getDir().addDirectory(graph);
		
	}
	
	
	public void getAttenuationCoefficients()
    {
			
    	int stripnum = 0;
    	int ijunk;
    	int counter = 0;
        Scanner scanner;
        //generted values
        try 
		{
			//scanner = new Scanner(new File("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/AttenCoeffSec4a.dat"));
        	scanner = new Scanner(new File("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/AttenCoeffm5run4294.dat"));
			while(scanner.hasNextInt())
			{
				if(counter < 68)
				{
					ijunk = scanner.nextInt();
					stripnum = scanner.nextInt();
					//umaxX[stripnum - 1] = scanner.nextDouble();
					genuA[stripnum - 1] = scanner.nextDouble();
					genuB[stripnum - 1] = scanner.nextDouble();
					genuC[stripnum - 1] = scanner.nextDouble();
					ijunk = scanner.nextInt();
					
					//genuA[stripnum - 1] *= 100.0/650.0;
					//genuC[stripnum - 1] *= 100.0/650.0;
				}
				else if(counter < 130)
				{
					ijunk = scanner.nextInt();
					stripnum = scanner.nextInt();
					//vmaxX[stripnum - 1] = scanner.nextDouble();
					genvA[stripnum - 1] = scanner.nextDouble();
					genvB[stripnum - 1] = scanner.nextDouble();
					genvC[stripnum - 1] = scanner.nextDouble();
					ijunk = scanner.nextInt();
					
					//genvA[stripnum - 1] *= 100.0/650.0;
					//genvC[stripnum - 1] *= 100.0/650.0;
				}
				else
				{
					ijunk = scanner.nextInt();
					stripnum = scanner.nextInt();
					//wmaxX[stripnum - 1] = scanner.nextDouble();
					genwA[stripnum - 1] = scanner.nextDouble();
					genwB[stripnum - 1] = scanner.nextDouble();
					genwC[stripnum - 1] = scanner.nextDouble();
					ijunk = scanner.nextInt();
					
					//genwA[stripnum - 1] *= 100.0/650.0;
					//genwC[stripnum - 1] *= 100.0/650.0;
				}
				++counter;
				
			}
			scanner.close();
		}
		catch (FileNotFoundException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
		try 
		{
			scanner = new Scanner(new File("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/UattenCoeff.dat"));
			while(scanner.hasNextInt())
			{
				stripnum = scanner.nextInt();
				umaxX[stripnum - 1] = scanner.nextDouble();
				uA[stripnum - 1] = scanner.nextDouble();
				uB[stripnum - 1] = scanner.nextDouble();
				uC[stripnum - 1] = scanner.nextDouble();
				
				
				ugain[stripnum - 1] = (uA[stripnum - 1] + uC[stripnum - 1]);
				//uA[stripnum - 1] *= ugain[stripnum - 1];
				//uC[stripnum - 1] *= ugain[stripnum - 1];
			}
			scanner.close();
		}
		catch (FileNotFoundException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try 
		{
			scanner = new Scanner(new File("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/VattenCoeff.dat"));
			while(scanner.hasNextInt())
			{
				stripnum = scanner.nextInt();
				vmaxX[stripnum - 1] = scanner.nextDouble();
				vA[stripnum - 1] = scanner.nextDouble();
				vB[stripnum - 1] = scanner.nextDouble();
				vC[stripnum - 1] = scanner.nextDouble();
				
				
				vgain[stripnum - 1] = (vA[stripnum - 1] + vC[stripnum - 1]);
				//vA[stripnum - 1] *= vgain[stripnum - 1];
				//vC[stripnum - 1] *= vgain[stripnum - 1];
			}
			scanner.close();
		}
		catch (FileNotFoundException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try 
		{
			scanner = new Scanner(new File("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/WattenCoeff.dat"));
			while(scanner.hasNextInt())
			{
				stripnum = scanner.nextInt();
				wmaxX[stripnum - 1] = scanner.nextDouble();
				wA[stripnum - 1] = scanner.nextDouble();
				wB[stripnum - 1] = scanner.nextDouble();
				wC[stripnum - 1] = scanner.nextDouble();
				
				
				wgain[stripnum - 1] = (wA[stripnum - 1] + wC[stripnum - 1]);
				//wA[stripnum - 1] *= wgain[stripnum - 1];
				//wC[stripnum - 1] *= wain[stripnum - 1];
			}
			scanner.close();
		}
		catch (FileNotFoundException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
    }

    
	public static void main(String[] args){    	
    	//PCALcalib detview = new PCALcalib("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/fc-muon-100k.evio");
    	//PCALcalib detview = new PCALcalib("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/fc-muon-500k.evio");
    	//PCALcalib detview = new PCALcalib("/home/chetry/PCAL/TayaPCAL/src/org/jlab/calib/fc-muon-3M-s2.evio");
    	
    	//Draws detector views
    	PCALcalibv2 detview = new PCALcalibv2();
    	
    	for(iteration = 0; iteration < detview.numiterations; ++iteration)
    	{
    		detview.getAttenuationCoefficients();
    		
	    	//initialize histograms that are sector and iteration dependent
	    	//sets up histograms A
	    	detview.init(); 
			   
	    	
	    	//fills sector and iteration dep. histogram
	    	//incorporate specfic cuts
			detview.process();
			  
			   
			
			//fits signal histograms
			//makes graph of centroids
			//fits graphs of centroids
			detview.Nickanalyze();
			detview.analyze();
			
			//calculate new gains and attenuation coefficients
			//detview.computegains();
    	}
		
    
		//output attenuation coefficients, gains, sector, and run number
		//detview.OutputStaticArrays();
		
		TBrowser browser = new TBrowser(detview.getDir()); //shows histograms  
		
    }




}
