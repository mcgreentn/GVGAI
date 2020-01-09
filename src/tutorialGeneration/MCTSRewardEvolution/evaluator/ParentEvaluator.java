package tutorialGeneration.MCTSRewardEvolution.evaluator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParentEvaluator {
	private String _inputFolder;
	private String _outputFolder;
	private String _signalFolder;

	public ParentEvaluator(String inputFolder, String outputFolder, String signalFolder) {
		this._inputFolder = inputFolder;
		this._outputFolder = outputFolder;
		this._signalFolder = signalFolder;
	}

	//write the input chromosomes (age, hasborder, and level) for the child to use and run
	public void writeChromosomes(String[] chromosomes, int[] children) throws FileNotFoundException, UnsupportedEncodingException {
		// how many chromosomes per child
		int chromesPerChild = chromosomes.length / (children.length);
		int remainingChromes = chromosomes.length % (children.length);
		int chromosomeCounter = 0;
		
		int count = 0;
        for (int child : children)
        {
            int extra = (count < remainingChromes) ? 1:0;
            // loop through the chromes
            for(int j = 0; j < chromesPerChild + extra; j++) {
            	PrintWriter writer = new PrintWriter(this._inputFolder + (child) + "_" + (chromosomeCounter) + ".txt", "UTF-8");
            	writer.print(chromosomes[chromosomeCounter++]);
            	writer.close();
            }
            System.out.println("** Child " + (child) + " will perform " + (chromesPerChild + extra) + " tasks.");
            // send request signal
            PrintWriter writer = new PrintWriter(this._signalFolder + child + ".request");
            writer.print("");
            writer.close();
            count++;
        }
	}

	//check if all of the children have finished running their simulations
	public boolean checkChromosomes(int[] children) {
		List<File> signals = new ArrayList<File>();
		for(int i : children) {
			File f = new File(this._signalFolder + i + ".response");
			if(!f.exists()) {
				return false;
			}
			signals.add(f);
		}
		// delete all that were found
		for(File f : signals) {
			f.delete();
		}
		return true;
	}
	
	public int[] checkChildrenAlive() {
		int i = 0;
		File childrenDir = new File(this._signalFolder);
		File[] childrenFiles = childrenDir.listFiles();
		ArrayList<Integer> childrenList = new ArrayList<Integer>();
		for (File child : childrenFiles) {
			if (child.getName().contains("response")){
				String name = child.getName();
				name = name.substring(0, name.indexOf("."));
				childrenList.add(Integer.parseInt(name));
				child.delete();
				i++;
			}
		}
		// Wacky transformations because java is java
		Integer[] tempChildren = new Integer[i];
		tempChildren = childrenList.toArray(tempChildren);
		int[] children = Arrays.stream(tempChildren).mapToInt(Integer::intValue).toArray();
		Arrays.sort(children);
		return children;
	}
	
	public void removeAwakeSignal() {
		File f = new File(this._signalFolder + "awake.request");
		if (f.exists()) {
			f.delete();
		}
	}

	//read back in the children's output results
	public String[] assignChromosomes() throws IOException {
		File output = new File(this._outputFolder);
		
		// get all files in output
		File[] listOfFiles = output.listFiles();
		String[] results = new String[listOfFiles.length];

		for(int i = 0; i < listOfFiles.length; i++) {
			results[i] = String.join("\n", Files.readAllLines(listOfFiles[i].toPath()));
		}
		return results;
	}
	

	//delete the old output chromosomes files
	public void clearOutputFiles(int size) {
		for(int i=0; i<size; i++) {
			File f = new File(this._outputFolder + i + ".txt");
			f.delete();
		}
	}
	
	public void sendRequests(int size) {
		for(int i=0; i<size; i++) {
			File f = new File(this._signalFolder + i + ".request");
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void sendAwakeRequest() {
		File f = new File(this._signalFolder + "awake.request");
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}