package Mains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import PatternMatching.SuffixTree;

public class SuffixTreeTest {
	/** Main Function **/
	public static void main(String[] args) throws IOException
	{ 
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		System.out.println("Suffix Tree Test\n");
		System.out.println("Enter string\n"); 
		String str = br.readLine(); 

		/** Construct Suffix Tree **/       
		SuffixTree st = new SuffixTree();
		st.T = str.toCharArray();
		st.N = st.T.length - 1;  

		for (int i = 0 ; i <= st.N ; i++ ){
			st.AddPrefix( st.active, i );
			System.out.println(i);
		}

		st.dump_edges( st.N );    
	}
}