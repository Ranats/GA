require "../Ruby/RealCodedMOGA.rb"

ga = RealCodedMOGA.new(population_size:50, finish_population:50, gene_size:100)

100.times do |i|
  ga.run(i)
end
