
# https://github.com/shikao-yuzu/RealCodedGA/blob/master/real-coded_GA.rb
class RealCodedGA

  def initialize(individual_size: 50, finish_population: 50)
    @individual_size = individual_size
    @finish_population = finish_population

    @population = Array.new(individual_size) {}
  end



end