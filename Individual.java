public class Individual implements Comparable<Individual>{
	private double[] genotype;
	private double[] mutation_genotype;
	private double[][] correlation_genotype;
	private double fitness;

	public Individual(double[] genotype, double[] mutation_genotype, double[][] correlation_genotype, double fitness) {
		super();
		this.genotype = genotype;
		this.mutation_genotype = mutation_genotype;
		this.correlation_genotype = correlation_genotype;
		this.fitness = fitness;
	}

	public int compareTo(Individual individual) {
		if (this.fitness > individual.fitness)
			return 1;
		else if (this.fitness == individual.fitness)
			return 0;
		else
			return -1;
	}

	public double[] getGenotype() {
		return this.genotype;
	}

	public double[] getMutationGenotype() {
		return this.mutation_genotype;
	}

	public double[][] getCorrelationGenotype() {
		return this.correlation_genotype;
	}

	public  double getFitness() {
		return this.fitness;
	}
}