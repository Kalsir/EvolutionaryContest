import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.Arrays;
import umontreal.ssj.randvarmulti.MultinormalPCAGen;
import umontreal.ssj.randvar.NormalGen;
import umontreal.ssj.rng.MRG32k3a;

public class player72 implements ContestSubmission
{
	Random rnd_;
	ContestEvaluation evaluation_;
	private int evaluations_limit_;

	// Parameters
	private int population_size = 100; //higher or lower may be better.
	private int generation_size = 100;

	private double beta = Math.PI / 18;
	private double tau_prime = 0.224; //proportional to 0.224
	private double tau = 0.398; //proportional to 0.398

	public player72()
	{
		rnd_ = new Random();
	}

	public void setSeed(long seed)
	{
		// Set seed of algortihms random process
		rnd_.setSeed(seed);
	}

	public void setEvaluation(ContestEvaluation evaluation)
	{
		// Set evaluation problem used in the run
		evaluation_ = evaluation;

		// Get evaluation properties
		Properties props = evaluation.getProperties();
		// Get evaluation limit
		evaluations_limit_ = Integer.parseInt(props.getProperty("Evaluations"));
		// Property keys depend on specific evaluation
		// E.g. double param = Double.parseDouble(props.getProperty("property_name"));
		boolean isMultimodal = Boolean.parseBoolean(props.getProperty("Multimodal"));
		boolean hasStructure = Boolean.parseBoolean(props.getProperty("Regular"));
		boolean isSeparable = Boolean.parseBoolean(props.getProperty("Separable"));

		// Do sth with property values, e.g. specify relevant settings of your algorithm
		if(isMultimodal){
			// Do sth
		}else{
			// Do sth else
		}
	}

	public void run()
	{
		// Initialize population and fill it with random individuals
		Individual population[] = new Individual[population_size];
		for(int i = 0; i < population_size; i++)
		{
			// Generate random genotype
			double genotype[] = new double[10];
			for(int j=0; j<10; j++)
			{
				genotype[j] = rnd_.nextDouble() * 10 - 5;
			}

			// Generate random mutation genotype
			double mutation_genotype[] = new double[10];
			for(int j=0; j<10; j++)
			{
				mutation_genotype[j] = 2;
			}

			// Generate zero correlation genotype
			double correlation_genotype[][] = new double[10][10];

			// Evaluate fitness
			double fitness = (double) evaluation_.evaluate(genotype);

			// Initialize individual and add it to the population
			population[i] = new Individual(genotype, mutation_genotype, correlation_genotype, fitness);
		}

		// We evaluated once for each starting individual
		int evals = population_size;

		// Sort population based on fitness
		Arrays.sort(population);

		// Report best score in initial population
		double best_score = population[population_size - 1].getFitness();
		System.out.println("Best initial score: " + Double.toString(best_score));

		MRG32k3a random_stream = new MRG32k3a();
		NormalGen normal_gen = new NormalGen(random_stream);

		// Run evolutionary algorithm
		while(evals<evaluations_limit_)
		{
			// Keep track of how many evaluations have been done
			if (evals % 10000 == 0)
				System.out.println(evals);
			evals+= generation_size;

			// Create array to hold offspring
			Individual offspring[] = new Individual[generation_size];

			// Create offspring
			for (int j = 0; j < generation_size; j++)
			{
				// Create child genotype
				double child_genotype[] = new double[10];
				double child_mutation_genotype[] = new double[10];
				double child_correlation_genotype[][] = new double[10][10];

				// Recombination
				for (int i = 0; i < 10; i++)
				{
					for (int k = 0; k < 4; k++)
						child_genotype[i] += population[population_size - k - 1].getGenotype()[i];
					child_genotype[i] /= 4;

					for (int k = 0; k < 4; k++)
						child_mutation_genotype[i] += population[population_size - k - 1].getMutationGenotype()[i];
					child_mutation_genotype[i] /= 4;

					for (int k = 0; k < 10; k++)
					{
						for (int z = 0; z < 4; z++)
							child_correlation_genotype[i][k] += population[population_size - k - 1].getCorrelationGenotype()[i][k];
						child_correlation_genotype[i][k] /= 4;
					}
				}

				// Update mutation genotype
				for (int i = 0; i < 10; i++){
					child_mutation_genotype[i] = child_mutation_genotype[i] * Math.pow(Math.E, rnd_.nextGaussian() * tau_prime + rnd_.nextGaussian() * tau);
				}

				// Update correlation matrix
				for (int i = 0; i < 10; i++)
					for (int k = 0; k < 10; k++)
						if (i != k)
						{
							child_correlation_genotype[i][k] += rnd_.nextGaussian() * beta;
							if (child_correlation_genotype[i][k] > Math.PI)
								child_correlation_genotype[i][k] -= 2 * Math.PI;
							if (child_correlation_genotype[i][k] < Math.PI)
								child_correlation_genotype[i][k] += 2 * Math.PI;
						}

				// Calculate covariance matrix
				double covariance_matrix[][] = new double[10][10];
				for (int i = 0; i < 10; i++)
					for (int k = 0; k < 10; k++)
					{
						if (i == k)
							covariance_matrix[i][k] = Math.pow(child_mutation_genotype[i], 2);
						else
							covariance_matrix[i][k] = (Math.pow(child_mutation_genotype[i], 2) - Math.pow(child_mutation_genotype[k], 2))/2*Math.tan(2*child_correlation_genotype[i][k]);
						//System.out.println(covariance_matrix[i][k]);
					}

				// Update genotype
				MultinormalPCAGen multivariate_normal_distribution = new MultinormalPCAGen(normal_gen, new double[10], covariance_matrix);
				double genotype_update[] = new double[10];
				multivariate_normal_distribution.nextPoint(genotype_update);
				for (int i = 0; i < 10; i++){
					//System.out.println(genotype_update[i]);
					child_genotype[i] = Math.max(-5, Math.min(5, child_genotype[i] + genotype_update[i]));
				}
				//System.exit(0);

				if(evals == 90000){
					for (int i = 0; i < 10; i++)
						System.out.println(child_mutation_genotype[i]);
				}

				// Evaluate new child
				double child_fitness = (double) evaluation_.evaluate(child_genotype);
				if (child_fitness > best_score) {
					best_score = child_fitness;
					System.out.println("Score update: " + Double.toString(best_score));
					for (int i = 0; i < 10; i++)
						System.out.println(child_mutation_genotype[i]);
				}
				offspring[j] = new Individual(child_genotype, child_mutation_genotype, child_correlation_genotype, child_fitness);
			}

			// Replace weakest individuals
			for (int j = 0; j < generation_size; j++)
				population[j] = offspring[j];

			// Resort population
			Arrays.sort(population);
		}
	}
}