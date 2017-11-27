module Settings
  module_function
  def individual_size; 100 end
  def finish_population; 200 end
end

class City
  attr_reader :x , :y
  def initialize(x,y)
    @x,@y = x,y
  end

  def distance_to(city)
    distance_x = (@x - city.x).abs
    distance_y = (@y - city.y).abs
    distance = Math.sqrt(distance_x**2 + distance_y**2)
  end
end

class Gene
  attr_reader :fitness, :chromosome
  attr_accessor :mutated

  def initialize(cities)
    @chromosome = cities.shuffle
    calculate_fitness
    @mutated = false
  end

  def self.init_child(chromosome)
    obj = Gene.new([])
    obj.set_chromosome(chromosome)
    return obj
  end

  def set_chromosome(chromosome)
    @chromosome = chromosome
  end

  def calculate_fitness
    @fitness = 0
    @chromosome.each_cons(2) do |city|
      @fitness += city[0].distance_to(city[1])
    end
  end

  def mutation
    m = [*0...@chromosome.size].sample(2)
    @chromosome[m[0]],@chromosome[m[1]] = @chromosome[m[1]], @chromosome[m[0]]
    @mutated = true
  end
end

# 交叉に使う親個体→ルーレット選択
# 次世代に残す個体→エリート選択：親世代のエリート個体と子世代の和集合
class GA
  attr_reader :elite, :population

  def initialize(population)
    @population = population
    @elite = @population.min_by{|pop| pop.fitness}
  end

  def roulette_select(fitness)
    roulette = rand(fitness)
    @population.inject(0) do |sum, gene|
      return gene if sum > roulette
      sum + gene.fitness
    end
    @population.last
  end

  def partially_mapped_crossover(p1,p2)
    max = p1.chromosome.size
    cr_point = rand(max-1) + 1
    chromosome = []
    order = []
    [p1,p2].each do |parent|
      chromosome << parent.chromosome.slice(0...cr_point)
      order << parent.chromosome.slice(cr_point..max)
    end

    order.each_with_index do |cross,i|
      cross.each do |bit|
        while chromosome[i].include?(bit)
          bit = chromosome[1-i][chromosome[i].index(bit)]
        end
        chromosome[i] << bit
      end
    end
=begin
    order_a1.each do |bit|
      while c1.include?(bit)
        bit = c2[c1.index(bit)]
      end
      c1 << bit
    end

    order_a2.each do |bit|
      while c2.include?(bit)
        bit = c1[c2.index(bit)]
      end
      c2 << bit
    end
=end
    return Gene.init_child(chromosome[0]) , Gene.init_child(chromosome[1])
  end

  def next_generation
    children = [@elite]
    fitness_sum = @population.inject(0){|g1,g2| g1 + g2.fitness}
    @population.sort_by! { |gene| gene.fitness }

    Settings.individual_size.times do |i|
      p1 = roulette_select(fitness_sum)
      p2 = roulette_select(fitness_sum)
      child = partially_mapped_crossover(p1,p2)
      child.each do |c|
        c.mutation if rand < 0.001
        c.calculate_fitness
      end
      children += child
    end
    @population = children.uniq{|gene| gene.chromosome}
    @elite = @population.min_by{|pop| pop.fitness}
  end
end

## main
# input
# n
# x1 y1
# x2 y2
# x3 y3
n = gets.to_i
cities = []
n.times do
  x,y = gets.split(" ").map(&:to_i)
  cities << City.new(x,y)
end

population = Array.new(Settings.individual_size){ Gene.new(cities)}

agent = GA.new(population)

Settings.finish_population.times do |i|
  agent.next_generation
end

agent.elite.chromosome.each do |city|
  puts "#{city.x} #{city.y}"
end