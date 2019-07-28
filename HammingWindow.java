package com.example.androidaudiorecorder;


/* TarsosDSP is developed by Joren Six at IPEM, University Ghent

 *

 * -------------------------------------------------------------

 *

 *  Info: http://0110.be/tag/TarsosDSP

 *  Github: https://github.com/JorenSix/TarsosDSP

 *  Releases: http://0110.be/releases/TarsosDSP/

 *

 *  TarsosDSP includes modified source code by various authors,

 *  for credits and info, see README.

 *

 */


/**

* A Hamming window function.

* 

* @author Damien Di Fede

* @author Corban Brook

* @see <a

*      href="http://en.wikipedia.org/wiki/Window_function#Hamming_window">The

*      Hamming Window</a>

* 

*/

public class HammingWindow extends WindowFunction {

	/** Constructs a Hamming window. */

	public HammingWindow() {

	}



	protected float value(int length, int index) {

		return 0.54f - 0.46f * (float) Math.cos(TWO_PI * index / (length - 1));

	}

}
















