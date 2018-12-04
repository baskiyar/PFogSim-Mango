function [] = plotAvgDistance()

    plotGenericResult(1, 1, 'Average Distance (m)', 'ALL_APPS', 0);
    plotGenericResult(1, 1, {'Average Distance';'for Augmented Reality App (m)'}, 'AUGMENTED_REALITY', 0);
    plotGenericResult(1, 1, 'Average Distance for Health App (m)', 'HEALTH_APP', 0);
    plotGenericResult(1, 1, 'Average Distance for Infotainment App (m)', 'INFOTAINMENT_APP', 0);
    plotGenericResult(1, 1, 'Average Distance for Heavy Comp. App (m)', 'HEAVY_COMP_APP', 0);
    
end
