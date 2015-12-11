package org.jlab.mon;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import org.jlab.clasrec.main.DetectorMonitoring;
import org.jlab.clasrec.rec.CLASMonitoring;
import org.jlab.clasrec.utils.ServiceConfiguration;
import org.root.histogram.*;
import org.root.pad.TCanvas;
import org.jlab.evio.clas12.EvioDataBank;
import org.jlab.evio.clas12.EvioDataEvent;
import org.root.func.F1D;
import org.root.group.TBrowser;
import org.root.group.TDirectory;

public class FCMon extends DetectorMonitoring 
{
	public String laba[] = {"monitor/pcal/adc","monitor/ecinner/adc","monitor/ecouter/adc"}; 
	//public String labt[] = {"monitor/pcal/tdc","monitor/ecinner/tdc","monitor/ecouter/tdc"}; 
	
	public FCMon()
	{
		super("FCMON","1.1.0","taya");
	}

	@Override
	public void analyze() 
	{
		//create directories
		TDirectory projection = new TDirectory("Projection");
		TDirectory expofit = new TDirectory("ExpoFit");
		TDirectory graph = new TDirectory("GraphE");
		
		double centroids[] = new double[500];
		
		double x[] = new double[500];
		double ex[] = new double[500];
		double ey[] = new double[500];
		double xTemp[]   = new double[500];
		double xTempEr[] = new double[500];
		double minx = 0.0;
		double maxx = 0.0;
		int counter = 0;
		
		//histograms
		H2D Hadc[] = new H2D[100];

		
		//projections
		H1D ProjHadc[] = new H1D[100];
		
		//fit function
		F1D expfit;

		String histName;
		String projName;
		String graphName;
		String plotName;
		String functionName;

		
		PrintWriter writer = null;
	
		//U,V,W strip calibration
		int count = 0;
		
		char stripLetter[] = {'u','v','w'};
		String histNameFormat[] = {"histU_%02d", "histV_%02d", "histW_%02d"};
		String projNameFormat[] = {"ProjHistU_W%02d", "ProjHistV_U%02d", "ProjHistW_U%02d"};
		String graphNameFormat[] = {"graphU_%02d", "graphV_%02d", "graphW_%02d"};
		String functionNameFormat[] = {"expU_%02d", "expV_%02d", "expW_%02d"};
		String fileName[] = {"UattenCoeff.dat", "VattenCoeff.dat", "WattenCoeff.dat"};
		
		int stripMax[] = {68, 62, 62};//u, v, w
		int crossStripMax[] = {62, 68, 68};//w, u, u
		
		for(int il = 0; il < 3; il++)
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
				Hadc[strip] = (H2D)getDir().getDirectory("crossStripHisto").getObject(histName);
				counter = 0;
				for(int crossStrip = 0; crossStrip < crossStripMax[il]; crossStrip++)
				{
					projName = String.format(projNameFormat[il], crossStrip + 1);
					ProjHadc[crossStrip] = Hadc[strip].sliceX(crossStrip);
					ProjHadc[crossStrip].setName(projName);
					
					projection.add(ProjHadc[crossStrip]);
					centroids[counter] = ProjHadc[crossStrip].getMean();
					if(centroids[counter] >= 1.0)
					{
						xTemp[counter] = CalcDistinStrips(stripLetter[il], crossStrip+1)[0];//calcDistinStrips
						xTempEr[counter] = CalcDistinStrips(stripLetter[il], crossStrip+1)[1];//calcDistinStrips
						
						x[counter] = CalcDistance(stripLetter[il], xTemp[counter], xTempEr[counter])[0];
						ex[counter] = CalcDistance(stripLetter[il], xTemp[counter], xTempEr[counter])[1];
						//centroids[counter] = ProjHadc[crossStrip].getMean();
						
						ey[counter] = 0.0;
						counter++;
					}
				}
				graphName = String.format(graphNameFormat[il], strip + 1);
				GraphErrors attengraph = graphn(graphName,counter,x,centroids,ex,ey);
				if(counter < 5)
				{
					minx = 500.0;
					maxx = 0.00;
				}
				else
				{
					minx = x[1];
					maxx = x[counter-2];
				}
				//Ucan.cd(count);
				//Ucan.draw(attengraph);
				
				//create function and fit
				functionName = String.format(functionNameFormat[il], strip + 1);
				expfit = new F1D("expC",maxx,minx);
				expfit.setName(functionName);
				expfit.parameter(0).setValue(100.0);
				expfit.setParLimits(0, -1000.0, 1000.0);
				expfit.parameter(1).setValue(-0.002659574468);
				expfit.parameter(2).setValue(10.0);
				
	
				attengraph.fit(expfit);
				//Ucan.draw(expfit,"same");
				graph.add(attengraph);
				expofit.add(expfit);
				
				//plotName = String.format("/home/chetry/EC_PCAL/coatjava/clasmon/src/org/jlab/mon/expfitU.png");
				//Ucan.save(plotname);
				
				//double chisq = (double)(expfit.getChiSquare(null)/expfit.getNDF());
				//System.out.println(chisq);
				int j = strip+1;
				writer.println(j + "   " + x[0] +"   "
						+ expfit.getParameter(0) +"   " 
						+ expfit.getParameter(1) +"   " 
						+ expfit.getParameter(2));
				//++count;
			}
			writer.close();
		}
		getDir().addDirectory(projection);
		getDir().addDirectory(expofit);
		getDir().addDirectory(graph);
	}

	@Override
	public void configure(ServiceConfiguration arg0) 
	{
		
	}
		
	public void init() 
	{
		int ustrip, vstrip, wstrip;// wstrip;
		
		String histNameFormat[] = {"histU_%02d", "histV_%02d", "histW_%02d"};
		String histname;
		
		int stripMax[] = {68, 62, 62};//u, v, w
		int binNum[] = {62,68,68};//w, u, u
		double binMaxX[] = {62.5,68.5,68.5};
	
		TDirectory geometry = new TDirectory("crossStripHisto");
		//declare histograms and add to directory
		for(int il = 0; il < 3; il++)
		{
			for(int strip = 0; strip < stripMax[il]; ++strip)
			{
				histname = String.format(histNameFormat[il], strip + 1);	
				geometry.add(new H2D(histname,binNum[il],0.5,binMaxX[il],100,0.0,300.0));
			}
		}
		getDir().addDirectory(geometry);
	}

	public float uvw_dalitz(int ic, int ip, int il) {
		float uvw=0;
		switch (ic) {
		case 0: //PCAL
			if (il==1&&ip<=52) uvw=(float)ip/84;
			if (il==1&&ip>52)  uvw=(float)(52+(ip-52)*2)/84;
			if (il==2&&ip<=15) uvw=(float) 2*ip/77;
			if (il==2&&ip>15)  uvw=(float)(30+(ip-15))/77;
			if (il==3&&ip<=15) uvw=(float) 2*ip/77;
			if (il==3&&ip>15)  uvw=(float)(30+(ip-15))/77;
			break;
		case 1: //ECALinner
			uvw=(float)ip/36;
			break;
		case 2: //ECALouter
			uvw=(float)ip/36;
			break;
		}
		return uvw;		
	}
	
	@Override
	public void processEvent(EvioDataEvent event) 
	{
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

		int thr            = 10; //threshold count
		int iis            = 2;	//Sector 5 hardwired for now
		
		H2D histadu[] = new H2D[68];
		
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
		double mc_t=0.0;
		if(event.hasBank("PCAL::true")==true)
		{
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
			float uvw=0;
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
            	float tdc =(float)(bank.getInt("TDC",i));
            	tdc=((tdc-(float)mc_t*1000)-tdcmax+1340000)/1000;
            	
                if(is==iis)//whatever sector mentioned
                {
            	   if (adc>thr) 
            	   {
            	     nh[is-1][il-1]++;
            	     int inh = nh[is-1][il-1];
            	     adcr[is-1][il-1][inh-1] = adc;
            	     //tdcr[is-1][il-1][inh-1] = tdc;
            	     strr[is-1][il-1][inh-1] = ip;
            	     uvw=uvw+uvw_dalitz(ic,ip,il);
            	   }
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
        	good_uvw[ic]  =      good_uv[ic]&good_lay[2+ic*3];
        	
        	good_uwt[ic]  =  good_uw[ic]&rs[2+ic*3]==rscutuw[ic];
        	good_vwt[ic]  =  good_uv[ic]&rs[0+ic*3]==rscutvw[ic];
        	good_wut[ic]  =  good_uw[ic]&rs[rsw[ic]+ic*3]==rscutwu[ic];
        	good_uwtt[ic] = good_uwt[ic]&ad[2+ic*3]>adcutuw[ic];
        	good_vwtt[ic] = good_vwt[ic]&ad[0+ic*3]>adcutvw[ic];
        	good_wutt[ic] = good_wut[ic]&ad[rsw[ic]+ic*3]>adcutwu[ic];        	
        }  
        
        String histNameFormat[] = {"histU_%02d", "histV_%02d", "histW_%02d"};
		String histname;
        for (int ic=0 ; ic<3 ; ic++)//ic = {PCAL, EC inner, EC outer}
        {
        	if (good_uvw[ic])//multiplicity
        	{
        		if(ic == 0)//PCAL
        		{
        			for(int il = 0; il < 3; il++)//three layers il = {u,v,w}
        			{
        				histname = String.format(histNameFormat[il], rs[il]);
            			histadu[rs[il]-1] = (H2D) getDir().getDirectory("crossStripHisto").getObject(histname);
            			if(il == 0)histadu[rs[il]-1].fill(rs[2],ad[il]);
            			else histadu[rs[il]-1].fill(rs[0],ad[il]);
        			} 
        		}
        	}
        }
    }
	
	//graphErrors constructor
	public GraphErrors graphn(String name, int numberpoints, double x[], double y[], double xe[], double ye[])
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
        
        GraphErrors mygraph = new GraphErrors(a,b,ae,be);
        mygraph.setName(name);
        mygraph.setTitle(name);
        
        return mygraph;
    }
	
	
	//crossstrip needs to be 1-62 or 1-68 not 0
    public double[] CalcDistinStrips(char stripletter, int crossstrip)
    {
	  double x=0;
	  double xE = 0.0;
        if(stripletter == 'u' || stripletter == 'U')
        {
            if(crossstrip <= 15)
            {
                //converts to 77 strips
                x = 2.0* crossstrip - 1.0;
                xE = 1.0;
            }
            else if(crossstrip > 15)
            {
                //converts to 77 strips
                x = (30.0 + (crossstrip - 15.0)) - 0.5;
                xE = 1.0/2.0;
            }
        }
        else if(stripletter == 'v' || stripletter == 'w' || stripletter == 'V' || stripletter == 'W')
        {
            if(crossstrip <= 52)
            {
                //converts to 84 strips
                x = crossstrip - 0.5;
                xE = 1.0/2.0;
                }
                else if(crossstrip > 52)
                {
                    //converts to 84 strips
                    x = (52.0 + 2.0*(crossstrip - 52.0)) - 1.0;
                    xE = 1.0;
                }
        }
        return new double[] {x, xE};
    }
    
    //xdistance needs to be 1-62 or 1-68 not 0
    public double[] CalcDistance(char stripletter, double xdistance, double xdistanceE)
    {
        int i;
        double distperstrip = 5.055;
        
        if(stripletter == 'u' || stripletter == 'U')
        {
            //convert strip number to distance
            xdistance = Math.abs(xdistance - 77.0) * distperstrip;
            xdistanceE = xdistanceE * distperstrip;
        }
        else if(stripletter == 'v' || stripletter == 'w' || stripletter == 'V' || stripletter == 'W')
        {
            //convert strip number to distance
            xdistance = Math.abs(xdistance - 84.0) * distperstrip;
            xdistanceE = xdistanceE * distperstrip;
        }
        return new double[] {xdistance, xdistanceE};
    }
    
	public static void main(String[] args)
	{
	   FCMon calib = new FCMon(); //declare class of FCMon
	   calib.init(); //access init function sets up histograms A
	   
	   //CLASMonitoring monitor = new CLASMonitoring("/home/chetry/EC_PCAL/coatjava/clasmon/src/org/jlab/mon/fc-muon-100k.evio", calib);
	   //CLASMonitoring monitor = new CLASMonitoring("/home/chetry/EC_PCAL/coatjava/clasmon/src/org/jlab/mon/fc-muon-500k-s2.evio", calib);
	   CLASMonitoring monitor = new CLASMonitoring("/home/chetry/EC_PCAL/coatjava/clasmon/src/org/jlab/mon/fc-muon-3M-s2.evio", calib);
	   
	   monitor.process();//fills histograms	   
	   calib.analyze();//works on created histograms: slices, fits, attenuation	   
	   TBrowser browser = new TBrowser(calib.getDir()); //shows histograms
	}
}
