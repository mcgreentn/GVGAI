package tutorialGeneration.MCTSRewardEvolution.evaluator;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
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
	public void writeChromosomes(String[] chromosomes, int childCount) throws FileNotFoundException, UnsupportedEncodingException {
		// how many chromosomes per child
		int chromesPerChild = chromosomes.length / (childCount);
		int remainingChromes = chromosomes.length % (childCount);
		int chromosomeCounter = 0;
		
        for (int i = 0; i < childCount; i++)
        {
            int extra = (i < remainingChromes) ? 1:0;
            // loop through the chromes
            for(int j = 0; j < chromesPerChild + extra; j++) {
            	PrintWriter writer = new PrintWriter(this._inputFolder + (i) + "_" + (chromosomeCounter) + ".txt", "UTF-8");
            	writer.print(chromosomes[chromosomeCounter++]);
            	writer.close();
            }
            System.out.println("** Child " + (i) + " will evaluate " + (chromesPerChild + extra) + " chromosomes.");
        }
	}

	//check if all of the children have finished running their simulations
	public boolean checkChromosomes(int size) {
		List<File> signals = new ArrayList<File>();
		for(int i=0; i<size; i++) {
			File f = new File(this._signalFolder + i + ".response");
			if(f.exists()) {
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
	
	public int checkChildrenAlive() {
		int i = 0;
		while(true) {
			File f = new File(this._signalFolder + i + ".response");
			i++;
			if(!f.exists()) {
				break;
			}
			f.delete();
		}
		// delete all and return
		return i;
	}

	//read back in the children's output results
	public String[] assignChromosomes(int size) throws IOException {
		String[] results = new String[size];
		for(int i=0; i<size; i++) {
			results[i] = String.join("\n", Files.readAllLines(Paths.get(this._outputFolder, i + ".txt")));
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
}