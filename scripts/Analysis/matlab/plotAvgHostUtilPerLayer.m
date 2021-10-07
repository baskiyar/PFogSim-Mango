function [] = plotAvgHostUtilPerLayer()

    stacked_histgraph(6000, 6, 'MACHINE_LEARNING', 'MACHINE LEARNING APP', 'Avg. host util. per fog layer (%)');
    stacked_histgraph(6000, 6, 'REMOTE_HEALTHCARE', 'REMOTE HEALTHCARE APP', 'Avg. host util. per fog layer (%)');
    stacked_histgraph(6000, 6, 'AUGMENTED_REALITY', 'AUGMENTED REALITY APP', 'Avg. host util. per fog layer (%)');
    stacked_histgraph(6000, 6, 'COGNITIVE_ASSISTANCE', 'COGNITIVE ASSISTANCE APP', 'Avg. host util. per fog layer (%)');
    
end