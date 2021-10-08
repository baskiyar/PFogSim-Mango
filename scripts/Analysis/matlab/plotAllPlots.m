function [] = plotAllPlots()
    folderPath = getConfiguration(1);
    allFiles = dir(strcat(folderPath,'/*DEVICES_*_GENERIC*'));
    oldFolder = cd(folderPath);
    allFiles = dir('**/*DEVICES_*_GENERIC*');
    cd(oldFolder);
    regex = 'DEVICES_([\w_\s]+)_GENERIC';
    allNames = [allFiles.name];
    allTypesList = regexp(allNames, regex, 'tokens');
    allTypes = string(allTypesList);
    typesList = unique(allTypes(:));
    for i=1:length(typesList)
        appType = allTypes(i);
        plotGenericResult(1, 8, 'Average VM Utilization (%)', appType, 0);
        plotGenericResult(1, 5, 'Service Time (sec)', appType, 0);
        plotGenericResult(2, 5, 'Service Time on Fog (sec)', appType, 0);
        plotGenericResult(3, 5, 'Service Time on Cloud (sec)', appType, 0);
        plotGenericResult(1, 6, 'Processing Time (sec)', appType, 0);
        plotGenericResult(2, 6, 'Processing Time on Cloudlet (sec)', appType, 0);
        plotGenericResult(3, 6, 'Processing Time on Cloud (sec)', appType, 0);
        plotGenericResult(1, 7, 'Average Network Delay (sec)', appType, 0);
        plotGenericResult(2, 7, 'Average WLAN Delay (sec)', appType, 0);
        plotGenericResult(3, 7, 'Average WAN Delay (sec)', appType, 0);
        plotGenericResult(4, 2, 'Average Hops', appType, 0);
        plotGenericResult(1, 2, 'Failed Tasks (%)', appType, 1);
        plotGenericResult(4, 1, 'Average Distance (m)', appType, 0);
        plotGenericResult(1, 9, 'Average Cost ($)', appType, 0);
%     %plotLocation();
    end
end