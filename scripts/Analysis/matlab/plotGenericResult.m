function plotOutput = plotGenericResult(rowOfset, columnOfset, yLabel, appType, calculatePercentage, config, graphTitle, yScale)
    if nargin < 6
        config = configuration.autoConfig();
    end
    if nargin < 7
        graphTitle = '';
    end
    if nargin < 8
        yScale = 'linear';
    end
    folderPath = config.FolderPath;
    numOfSimulations = config.IterationCount;
    stepOfxAxis = config.XAxisStep;
    scenarioType = config.SimulationScenarioList;
    startOfMobileDeviceLoop = config.MinimumMobileDevices;
    stepOfMobileDeviceLoop = config.MobileDeviceStep;
    endOfMobileDeviceLoop = config.MaximumMobileDevices;
    numOfMobileDevices = (endOfMobileDeviceLoop - startOfMobileDeviceLoop)/stepOfMobileDeviceLoop + 1;

    all_results = zeros(size(scenarioType,1), numOfMobileDevices, numOfSimulations);
    min_results = zeros(size(scenarioType,1), numOfMobileDevices);
    max_results = zeros(size(scenarioType,1), numOfMobileDevices);
    
    if ~exist('appType','var')
        appType = 'ALL_APPS';
    end
    
    for s=1:numOfSimulations
        for i=1:size(scenarioType,1)
            for j=1:numOfMobileDevices
                mobileDeviceNumber = startOfMobileDeviceLoop + stepOfMobileDeviceLoop * (j-1);
                fileName = strcat('**/*SIMRESULT_*',char(scenarioType(i)),'*_NEXT_FIT_*',int2str(mobileDeviceNumber),'*DEVICES_*',appType,'*_GENERIC.log');
                oldFolder = cd(folderPath);
                allFiles = dir(fileName);
                cd(oldFolder);
                if s>length(allFiles)
                    error(strcat('Error: SIMRESULT files missing. Iterations expected: ', numOfSimulations, '. Iterations found: ', length(allFiles), '.'))
                end
                filePath = strcat(allFiles(s).folder, '/', allFiles(s).name);
                fileData = readmatrix(filePath, 'Delimiter', ';','Range', rowOfset+1);
                value = fileData(1,columnOfset);
                if(calculatePercentage==1)
                    totalTask = fileData(1,1)+fileData(1,2);
                    value = (100 * value) / totalTask;
                end
                all_results(i,j,s) = value;
            end
        end
    end
    
    if(numOfSimulations == 1)
        results = all_results;
    else
        results = mean(all_results, 3); %still 3d matrix but 1xMxN format
    end
    
    results = squeeze(results); %remove singleton dimensions
    
    for i=1:size(scenarioType,1)
        for j=1:numOfMobileDevices
            x=results(i,j,:);                    % Create Data
            SEM = std(x)/sqrt(length(x));            % Standard Error
            ts = tinv([0.05  0.95],length(x));   % T-Score
            CI = mean(x) + ts*SEM;                   % Confidence Intervals

            if(CI(1) < 0)
                CI(1) = 0;
            end

            if(CI(2) < 0)
                CI(2) = 0;
            end

            min_results(i,j) = results(i,j) - CI(1);
            max_results(i,j) = CI(2) - results(i,j);
        end
    end
    

    types = zeros(1,numOfMobileDevices);
    for i=1:numOfMobileDevices
        types(i)=startOfMobileDeviceLoop+((i-1)*stepOfMobileDeviceLoop);
    end
    
    
    hFig = figure;
    set(hFig, 'Position', config.PlotWindowCoordinates);
    set(0,'DefaultAxesFontName','Times New Roman');
    set(0,'DefaultTextFontName','Times New Roman');
    set(0,'DefaultAxesFontSize',12);
    set(0,'DefaultTextFontSize',12);
    if(config.ColorPlot == 1)
        for i=stepOfxAxis:stepOfxAxis:numOfMobileDevices
            xIndex=startOfMobileDeviceLoop+((i-1)*stepOfMobileDeviceLoop);
            
            markers = config.LineStyleColor;
            for j=1:size(scenarioType,1)
                if isempty(yScale)
                    yScale = 'linear';
                end
                if strcmp(yScale,'log')
                    semilogy(xIndex, max(1, results(j,i)),char(markers(j)),'MarkerEdgeColor',config.LineColors(j,:),'color',config.LineColors(j,:));  
                elseif strcmp(yScale,'linear')  
                    plot(xIndex, results(j,i),char(markers(j)),'MarkerFaceColor',config.LineColors(j,:),'color',config.LineColors(j,:));
                end
                hold on;
            end
        end
        
        for j=1:size(scenarioType,1)
            if(config.IncludeErrorBars == 1)
                errorbar(types, results(j,:), min_results(j,:),max_results(j,:),':k','color',config.LineColors(j,:),'LineWidth',1.5);
            else
                plot(types, results(j,:),':k','color',config.LineColors(j,:),'LineWidth',1.5);
            end
            hold on;
        end
    else
        markers = config.LineStyleMono;
        for j=1:size(scenarioType,1)
            if(config.IncludeErrorBars == 1 && config.IterationCount > 1)
                errorbar(types, results(j,:),min_results(j,:),max_results(j,:),char(markers(j)),'MarkerFaceColor','w','LineWidth',1.4);
            else
                if strcmp(yScale, 'log')
                    semilogy(types, max(1, results(j,:)), char(markers(j)), 'MarkerFaceColor', 'w', 'LineWidth', 1.4);
                else
                    plot(types, results(j,:),char(markers(j)),'MarkerFaceColor','w','LineWidth',1.4);
                end
            end
            hold on;
        end
    end
    lgnd = legend(config.ScenarioLabelsList,'Location','best');
    if(config.ColorPlot == 1)
        set(lgnd,'color','none');
    end
    
    hold off;
    axis square
    xlabel(config.HorizontalAxisLabel);
    set(gca,'XTick', (stepOfxAxis*stepOfMobileDeviceLoop):(stepOfxAxis*stepOfMobileDeviceLoop):endOfMobileDeviceLoop);
    ylabel(yLabel);
    set(gca,'XLim',[startOfMobileDeviceLoop-5 endOfMobileDeviceLoop+5]);
    
    set(get(gca,'Xlabel'),'FontSize',12)
    set(get(gca,'Ylabel'),'FontSize',12)
    set(lgnd,'FontSize',12)
    if isempty(graphTitle)
        graphTitle = strcat(yLabel, ' - ', strrep(appType, '_', ' '));
    end
    title(graphTitle, 'FontSize', 12);
    annotation('rectangle',[0 0 1 1],'Color','w');
    plotOutput = hFig;
end