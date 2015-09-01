package Mains;

import Embodiment.PhysicalConstraints_ShimonComplete;
public class MainTest {
	

	static PhysicalConstraints_ShimonComplete constraints;
	public static void main(String args[]) {	
		
		int n = Runtime.getRuntime().availableProcessors();
		System.out.println("available processors " + n);
		constraints = new PhysicalConstraints_ShimonComplete();

	}

}
