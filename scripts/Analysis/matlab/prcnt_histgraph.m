function [] = prcnt_histgraph(devCount, rowIndex, appType, graphTitle, yAxisLabel)
    folderPath = getConfiguration(1);
    scenarioType = getConfiguration(5);
    value = zeros(size(scenarioType, 2), 4);
    disp(value);
    try
        for i=1:size(scenarioType, 2)
            disp(i);
            filePath = strcat(folderPath,'\NONMOVING1\SIMRESULT_',char(scenarioType(i)),'_NEXT_FIT_',int2str(devCount),'DEVICES_',appType,'_GENERIC.log');                
            disp(filePath);
            numtasks = dlmread(filePath,';',[rowIndex 0 rowIndex 0]);
            disp(numtasks);
            m = dlmread(filePath,';',[rowIndex 1 rowIndex 4]);
            %disp(m);
            for j=1:size(m, 2)
                %disp(j);
                %value(i, j) = m(j);
                value(i, j) = (100 * m(j))/numtasks;
                %disp(value(i, j))
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
    l = cell(1,4);
    l{1}='Mobility'; l{2}='Node Capacity'; l{3}='Net. Bandwidth'; l{4}='Net. Latency';    
    lgnd = legend(h,l);
    set(gca, 'XTickLabel', getConfiguration(6),'FontSize',10);
    %xlabel('Orchestrators');
    ylabel(yAxisLabel);
    set(get(gca,'Xlabel'),'FontSize',12)
    set(get(gca,'Ylabel'),'FontSize',12)
    set(lgnd,'FontSize',10)
    title(graphTitle, 'FontSize', 10)
end
