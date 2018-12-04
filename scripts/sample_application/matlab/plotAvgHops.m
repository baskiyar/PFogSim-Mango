function [] = plotAvgHops()

    plotGenericResult(1, 1, 'Average Hops', 'ALL_APPS', 0);
    plotGenericResult(1, 1, {'Average Hops';'for Augmented Reality App'}, 'AUGMENTED_REALITY', 0);
    plotGenericResult(1, 1, 'Average Hops for Health App', 'HEALTH_APP', 0);
    plotGenericResult(1, 1, 'Average Hops for Infotainment App', 'INFOTAINMENT_APP', 0);
    plotGenericResult(1, 1, 'Average Hops for Heavy Comp. App', 'HEAVY_COMP_APP', 0);
    
end
