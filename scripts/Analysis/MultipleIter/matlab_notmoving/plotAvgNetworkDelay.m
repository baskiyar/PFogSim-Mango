function [] = plotAvgNetworkDelay()


    %plotGenericResult(1, 7, 'Avg. network delay (sec)', 'ALL_APPS', 0, 'ALL APPS', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (sec)'}, 'MACHINE_LEARNING', 0, 'MACHINE LEARNING APP', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (sec)'}, 'REMOTE_HEALTHCARE', 0, 'REMOTE HEALTHCARE APP', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (sec)'}, 'COGNITIVE_ASSISTANCE', 0, 'COGNITIVE ASSISTANCE APP', 'linear');
    plotGenericResult(1, 7, {'Avg. network delay (sec)'}, 'AUGMENTED_REALITY', 0, 'AUGMENTED REALITY APP', 'linear');

end
