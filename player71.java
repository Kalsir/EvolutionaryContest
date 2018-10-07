import org.vu.contest.ContestSubmission;
import org.vu.contest.ContestEvaluation;

import java.util.Random;
import java.util.Properties;
import java.util.Arrays;
import umontreal.ssj.randvarmulti.MultinormalPCAGen;
import umontreal.ssj.randvar.NormalGen;
import umontreal.ssj.rng.MRG32k3a;

public class player71 implements ContestSubmission
{
	Random rnd_;
	ContestEvaluation evaluation_;
	private int evaluations_limit_;

	// Parameters
	private int population_size = 25; //higher or lower may be better.
	private int generation_size = 175;

	private double beta = Math.PI / 90 * 5;
	private double tau_prime = 0.224 * 2; //proportional to 0.224
	private double tau = 0.398 * 2; //proportional to 0.398
	//private double starting_sigma = 0.1;

	public player71()
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
				mutation_genotype[j] = rnd_.nextDouble();
			}

			// Generate zero correlation genotype
			double correlation_genotype[][] = new double[10][10];

			// Evaluate fitness
			//double fitness = (double) evaluation_.evaluate(genotype);

			// Initialize individual and add it to the population
			population[i] = new Individual(genotype, mutation_genotype, correlation_genotype, 0);
		}

		// We evaluated once for each starting individual
		int evals = 0;

		// Report best score in initial population
		double best_score = population[population_size - 1].getFitness();
		System.out.println("Best initial score: " + Double.toString(best_score));

		MRG32k3a random_stream = new MRG32k3a();
		NormalGen normal_gen = new NormalGen(random_stream);

		// Run evolutionary algorithm
		while(evals<evaluations_limit_ - generation_size)
		{
			// Keep track of how many evaluations have been done
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

				// Pick two parents uniform randomly
				int parent1 = rnd_.nextInt(population_size);
				int parent2 = parent1;
				while (parent2 == parent1)
					parent2 = rnd_.nextInt(population_size);

				// Recombination
				for (int i = 0; i < 10; i++)
				{				
					child_genotype[i] += population[parent1].getGenotype()[i];
					child_genotype[i] += population[parent2].getGenotype()[i];
					child_genotype[i] /= 2;
					
					child_mutation_genotype[i] += population[parent1].getMutationGenotype()[i];
					child_mutation_genotype[i] += population[parent2].getMutationGenotype()[i];
					child_mutation_genotype[i] /= 2;

					for (int k = 0; k < 10; k++)
					{
						child_correlation_genotype[i][k] += population[parent1].getCorrelationGenotype()[i][k];
						child_correlation_genotype[i][k] += population[parent2].getCorrelationGenotype()[i][k];
						child_correlation_genotype[i][k] /= 2;
					}
				}

				// Update mutation genotype
				for (int i = 0; i < 10; i++){
					child_mutation_genotype[i] = Math.max((10 - best_score)/Math.pow(10,20), child_mutation_genotype[i] * Math.pow(Math.E, rnd_.nextGaussian() * tau_prime + rnd_.nextGaussian() * tau));
				}

				// Update correlation matrix
				for (int i = 0; i < 10; i++)
					for (int k = 0; k < 10; k++)
						if (i > k)
						{
							child_correlation_genotype[i][k] += rnd_.nextGaussian() * beta;
							if (child_correlation_genotype[i][k] > Math.PI)
								child_correlation_genotype[i][k] -= 2 * Math.PI;
							if (child_correlation_genotype[i][k] < -Math.PI)
								child_correlation_genotype[i][k] += 2 * Math.PI;
							child_correlation_genotype[k][i] = child_correlation_genotype[i][k];
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

				/*if(evals == 90000){
					for (int i = 0; i < 10; i++)
						System.out.println(child_mutation_genotype[i]);
				}*/

				// Evaluate new child
				double child_fitness = (double) evaluation_.evaluate(child_genotype);
				if (child_fitness > best_score) {
					best_score = child_fitness;
					System.out.println("Score update: " + Double.toString(best_score));
					System.out.println(evals);
					for (int i = 0; i < 10; i++)
						System.out.println(child_mutation_genotype[i]);
					System.out.println("Genotype:");
					for (int i = 0; i < 10; i++)
						System.out.println(child_genotype[i]);
					System.out.println("correlation");
					System.out.println(child_correlation_genotype[4][5]);
					System.out.println("Last update");
					for (int i = 0; i < 10; i++)
						System.out.println(genotype_update[i]);
					//if (best_score > 9.83)
						//System.exit(0);
				}
				offspring[j] = new Individual(child_genotype, child_mutation_genotype, child_correlation_genotype, child_fitness);
			}
			// Resort population
			Arrays.sort(offspring);
			Arrays.sort(population);
			//Individual best_individual = population[population_size - 1];

			if (best_score > 9.99)
			{
				//tau_prime = 0.224 * 4; //proportional to 0.224
				//tau = 0.398 * 4; //proportional to 0.398
				/*int offspring_counter = generation_size - 1;
				int population_counter = population_size - 1;
				Individual new_population[] = new Individual[population_size];
				for (int j = 0; j < population_size; j++)
				{
					if (offspring[offspring_counter].getFitness() > population[population_counter].getFitness())
					{
						new_population[j] = offspring[offspring_counter];	
						offspring_counter--;
					}
					else
					{
						new_population[j] = population[population_counter];	
						population_counter--;
					}
				}
				population = new_population;*/
			}
			//else
			//{
				// Replace weakest individuals
				for (int j = 0; j < population_size; j++)
					population[j] = offspring[generation_size - 1 - j];				
			//}	
		}
	}
}