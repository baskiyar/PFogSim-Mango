function [] = plotAvgMessages()

    %plotGenericResult(4, 4, 'Avg. no. of messages', 'ALL_APPS', 0, 'ALL APPS', 'linear');
    plotGenericResult(4, 4, {'Avg. no. of messages'}, 'MACHINE_LEARNING', 0, 'MACHINE LEARNING APP', 'log');
    plotGenericResult(4, 4, {'Avg. no. of messages'}, 'REMOTE_HEALTHCARE', 0, 'REMOTE HEALTHCARE APP', 'log');
    plotGenericResult(4, 4, {'Avg. no. of messages'}, 'COGNITIVE_ASSISTANCE', 0, 'COGNITIVE ASSISTANCE APP', 'log');
    plotGenericResult(4, 4, {'Avg. no. of messages'}, 'AUGMENTED_REALITY', 0, 'AUGMENTED REALITY APP', 'log');
    
end