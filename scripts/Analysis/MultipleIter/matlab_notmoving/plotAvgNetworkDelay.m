function [] = plotAvgNetworkDelay()


    %plotGenericResult(1, 7, 'Avg. network delay (msec)', 'ALL_APPS', 0, 'ALL APPS', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (msec)'}, 'MACHINE_LEARNING', 0, 'MACHINE LEARNING APP', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (msec)'}, 'REMOTE_HEALTHCARE', 0, 'REMOTE HEALTHCARE APP', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (msec)'}, 'COGNITIVE_ASSISTANCE', 0, 'COGNITIVE ASSISTANCE APP', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (msec)'}, 'AUGMENTED_REALITY', 0, 'AUGMENTED REALITY APP', 'linear');

end
