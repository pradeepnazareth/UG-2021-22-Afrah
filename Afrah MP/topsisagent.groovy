import org.arl.fjage.*
import org.arl.fjage.param.Parameter
import org.arl.unet.*


  class topsisagent extends UnetAgent {

  enum Params implements Parameter {        
    //declare any parameters here
     nodeval, order
   }

  final String title = 'TOPSIS agent'        
  final String description = 'Input: List of nodes with attributes, Output: Node ranking according to TOPSIS score' 

  
    
    LinkedHashMap nodeval = [];
     LinkedHashMap order = []; //nodes sorted according to TOPSIS 
  
  def topsisalg(info,Nf) { 
 
   println( 'Applying TOPSIS on : ' + info )
 
   LinkedHashMap scores  = []
 
  if(info.size()==1)  //only one alternative
  {
     for(entry in info)
      {scores[entry.key]=1.0}
      
      return scores
  }
 
 
    def fsum=new float [Nf];
    def Entropy=new float [Nf];
    def weights=new float [Nf];
    def best=new float[Nf]
    def worst=new float[Nf]
    
 //STEP -1 : To normalise the columns
 
 //1.1 Store the sum of each column in fsum
 
 for (entry in info) {
 for (int i = 0; i < Nf; i++) {
 fsum[i] = fsum[i] +   entry.value[i] } }
    
    //println( 'nodeval after step 1: summing topsis: ' + nodeval )

    
    
 //1.2 Divide the values in each column with their respective sum
 for (entry in info) {
 for (int i = 0; i < Nf; i++) {
     
     //System.out.println( 'attribute value =' + (float)info[entry.key][i] + '/' + ' fsum[i] = ' + fsum[i] )
      
     info[entry.key][i]=((float)info[entry.key][i]) / fsum[i] 
     
     //System.out.println( 'norm value' + info[entry.key][i] )
     
 } }
 
 
 
 //System.out.println(Arrays.toString(fsum))
 
 
 
   

  


// Step-2 To calculate weights using entropy formula
 
  //2.1 Find Entropy using entropy formula
  
  for (int i = 0; i < Nf; i++){
  for (entry in info) {
  Entropy[i] = Entropy[i] +   entry.value[i] *  Math.log(entry.value[i]) }
  Entropy[i]= (-1 * Entropy[i]) / Math.log(info.size()) }
 
  //System.out.println('Entropy : ' + Arrays.toString(Entropy))
  
  // 2.2  Calculate Weights from the respective entropies 
 
  for(int j = 0 ; j < Nf; j++)
  {weights[j]=(1-Entropy[j])/(Nf-Entropy.sum())}
  
  

// Step-2 ends


System.out.println('Weights : ' + Arrays.toString(weights) )


// Step-3 : Mutiply the attribute values with their respective weights

 for (entry in info) { for (int i = 0; i < Nf; i++) {
 info[entry.key][i]=info[entry.key][i] * weights[i] } }
 
 //Step-3 ends




// Step-4 : Find the best and the worst alternative

for (int i = 0; i < Nf; i++) 
 {  best[i] = -2
    worst[i]=  2
    for(entry in info){
         if (entry.value[i]>best[i])
             best[i]=entry.value[i]
         if (entry.value[i]<worst[i])
             worst[i]=entry.value[i] }}



//Step-4 Ends




float dfb=0
float dfw=0


//Step-5 : Calculating TOPSIS score
// 5.1 : Calculate the Distance from the Best and the Worst
// 5.2  : Find TOPSIS score
for(entry in info)
{  dfb=0
   dfw=0
for (int i = 0; i < Nf; i++) 
 { dfb  = dfb  + (entry.value[i] - best[i]) * (entry.value[i] - best[i])
   dfw  = dfw  + (entry.value[i] - worst[i]) * (entry.value[i] - worst[i])
  } 
    dfb =  Math.sqrt(dfb)
    dfw =  Math.sqrt(dfw)
   scores[entry.key] = dfw / ( dfw + dfb )    
}





// Step-6 : Sort the alternatives according to their TOPSIS score
   scores = scores.sort {a, b -> b.value <=> a.value}
  
   return scores; }
  
  
  
  
   @Override
  void startup() {
    
    subscribeForService(Services.DATAGRAM);
   subscribeForService(Services.PHYSICAL);
   
    
     add new TickerBehavior(60000, {   // topsis function is called every one minute
        
        
         LinkedHashMap info = [];
        
             for (entry in nodeval)
         { 
             
             info[entry.key] = [entry.value[0],entry.value[1],entry.value[2]]
           
          }

        
        
        
        order = topsisalg(info,3)  //function call for TOPSIS
        
        
        
        println('Nodes Sorted according to their TOPSIS Score:' + order)
        
        
        for (entry in order)
        {   System.out.println(Integer.toString(entry.key) + ' : ' + Double.toString(entry.value));} })}
  
  
  
  
  
  
  
  
  
@Override
  void processMessage(Message msg) {
      
       
          if (msg instanceof DatagramNtf && msg.protocol == Protocol.DATA) //Received attributes from other nodes ?
    {
                  
                   def node = agentForService(Services.NODE_INFO);
                  
                  
              
                  
                  
                  String str=new String(msg.data) ;
                  String [] strarray; 
                  strarray = str.split(' ');
     
                 
                  println(node.nodeName + ' received attributes ' + strarray  + ' from ' + msg.from)
      
                
                      
                  println('nodeval before updation' + nodeval )
                  
                  nodeval[msg.from]=[ Float.valueOf(strarray[0]) , Float.valueOf(strarray[1]) , Float.valueOf(strarray[2]), Float.valueOf(strarray[3]), Float.valueOf(strarray[4])   ]
                  
                  
                  log.info('Updated neighbor attributes at ' + node.nodeName);
                  
                  println('nodeval after updation' + nodeval )
                  
                  
                  
  
    
    }

  }

  List<Parameter> getParameterList() { allOf(Params)}
  
}