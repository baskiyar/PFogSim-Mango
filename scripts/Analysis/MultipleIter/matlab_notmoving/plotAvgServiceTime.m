function [] = plotAvgServiceTime()

    %plotGenericResult(1, 5, 'Avg. service time (msec)', 'ALL_APPS', 0, 'ALL APPS', 'linear');
    plotGenericResult(1, 5, 'Avg. service time (msec)', 'MACHINE_LEARNING', 0, 'MACHINE LEARNING APP', 'linear');
    plotGenericResult(1, 5, 'Avg. service time (msec)', 'REMOTE_HEALTHCARE', 0, 'REMOTE HEALTHCARE APP', 'linear');
    plotGenericResult(1, 5, 'Avg. service time (msec)', 'COGNITIVE_ASSISTANCE', 0, 'COGNITIVE ASSISTANCE APP', 'linear');
    plotGenericResult(1, 5, 'Avg. service time (msec)', 'AUGMENTED_REALITY', 0, 'AUGMENTED REALITY APP', 'linear');

end
