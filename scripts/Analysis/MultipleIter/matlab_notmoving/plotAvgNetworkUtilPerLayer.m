function [] = plotAvgNetworkUtilPerLayer()

    stacked_histgraph(6000, 7, 'MACHINE_LEARNING', 'MACHINE LEARNING APP', 'Avg. network util per fog layer (%)');
    stacked_histgraph(6000, 7, 'REMOTE_HEALTHCARE', 'REMOTE HEALTHCARE APP', 'Avg. network util per fog layer (%)');
    stacked_histgraph(6000, 7, 'AUGMENTED_REALITY', 'AUGMENTED REALITY APP', 'Avg. network util per fog layer (%)');
    stacked_histgraph(6000, 7, 'COGNITIVE_ASSISTANCE', 'COGNITIVE ASSISTANCE APP', 'Avg. network util per fog layer (%)');
     
end