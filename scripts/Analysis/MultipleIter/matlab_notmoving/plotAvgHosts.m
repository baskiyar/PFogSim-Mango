function [] = plotAvgHosts()

    %plotGenericResult(4, 3, 'Avg. no. of hosts searched', 'ALL_APPS', 0, 'ALL APPS', 'log');
    plotGenericResult(4, 3, {'Avg. no. of hosts searched'}, 'MACHINE_LEARNING', 0, 'MACHINE LEARNING APP', 'log');
    plotGenericResult(4, 3, {'Avg. no. of hosts searched'}, 'REMOTE_HEALTHCARE', 0, 'REMOTE HEALTHCARE APP', 'log');
    plotGenericResult(4, 3, {'Avg. no. of hosts searched'}, 'COGNITIVE_ASSISTANCE', 0, 'COGNITIVE ASSISTANCE APP', 'log');
    plotGenericResult(4, 3, {'Avg. no. of hosts searched'}, 'AUGMENTED_REALITY', 0, 'AUGMENTED REALITY APP', 'log');
    
end