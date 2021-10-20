function [] = plotTaskCount()

    stacked_histgraph(6000, 5, 'MACHINE_LEARNING', 'MACHINE LEARNING APP', 'No. of tasks executed per fog layer');
    stacked_histgraph(6000, 5, 'REMOTE_HEALTHCARE', 'REMOTE HEALTHCARE APP', 'No. of tasks executed per fog layer');
    stacked_histgraph(6000, 5, 'AUGMENTED_REALITY', 'AUGMENTED REALITY APP', 'No. of tasks executed per fog layer');
    stacked_histgraph(6000, 5, 'COGNITIVE_ASSISTANCE', 'COGNITIVE ASSISTANCE APP', 'No. of tasks executed per fog layer');
    
end