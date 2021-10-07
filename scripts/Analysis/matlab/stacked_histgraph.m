function [] = stacked_histgraph(devCount, rowIndex, appType, graphTitle, yAxisLabel)
    folderPath = getConfiguration(1);
    scenarioType = getConfiguration(5);
    value = zeros(size(scenarioType, 2), 7);
    disp(value);
    try
        for i=1:size(scenarioType, 2)
            disp(i);
            filePath = strcat(folderPath,'\NONMOVING1\SIMRESULT_',char(scenarioType(i)),'_NEXT_FIT_',int2str(devCount),'DEVICES_',appType,'_GENERIC.log');                
            disp(filePath);
            m = dlmread(filePath,';',[rowIndex 0 rowIndex 6]);
            disp(m);
            for j=1:size(m, 2)
                disp(j);
                value(i, j) = m(j);
            end
        end
    catch err
        error(err);
    end
    disp(value)
    
    %create bar graph
    %figure(devCount)
    
    hFig = figure;
    set(hFig, 'Position',getConfiguration(7));

    
    h = bar(value,1);
    l = cell(1,7);
    l{1}='FL-1'; l{2}='FL-2'; l{3}='FL-3'; l{4}='FL-4'; l{5}='FL-5'; l{6}='FL-6'; l{7}='FL-7';    
    lgnd = legend(h,l);
    set(gca, 'XTickLabel', getConfiguration(6),'FontSize',12);
    %xlabel('Orchestrators');
    ylabel(yAxisLabel);
    set(get(gca,'Xlabel'),'FontSize',12)
    set(get(gca,'Ylabel'),'FontSize',12)
    set(lgnd,'FontSize',10)
    title(graphTitle, 'FontSize', 10)
end
