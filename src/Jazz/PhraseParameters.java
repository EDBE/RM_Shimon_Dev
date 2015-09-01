package Jazz;

public class PhraseParameters {
	
	
	//pitch features
	public float[] pitchContours = new float[1000];
	public float pitchWeight= 1;
	public float[] colorContours = new float[1000];
	public float colorWeight= 1;
	public float[] harmonicTensionContours = new float[1000];
	public float harmonicTensionWeight= 1;
	
	//rhythmic reatures
	public float[] complexityContours = new float[1000];
	public float complexityWeight= 1;
	public float[] densityContours = new float[1000];
	public float densityWeight= 1;
	
	public boolean readMelody = true; //true if to read a melody from a score and play to best of ability, false for improvising a melody
	
	public int bpm=120; //default tempo
	
	//locations
	public int startMeasure,startBeat, endMeasure,endBeat;
	
	//constraints info
	public int constraints=1;
	
	
	public PhraseParameters(){
		
		
		
	}

	
}
