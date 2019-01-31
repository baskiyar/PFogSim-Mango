function [] = plotAvgCost()

    plotGenericResult(1, 9, 'Average Cost ($)', 'ALL_APPS', 0);
    plotGenericResult(1, 9, {'Average Cost';'for Augmented Reality App ($)'}, 'AUGMENTED_REALITY', 0);
    plotGenericResult(1, 9, 'Average Cost for Health App ($)', 'HEALTH_APP', 0);
    plotGenericResult(1, 9, 'Average Cost for Infotainment App ($)', 'INFOTAINMENT_APP', 0);
    plotGenericResult(1, 9, 'Average Cost for Heavy Comp. App ($)', 'HEAVY_COMP_APP', 0);
    
end
