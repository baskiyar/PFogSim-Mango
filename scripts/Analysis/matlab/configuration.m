classdef configuration
    %CONFIGURATION Access configuration parameters for plotting simulation.
    %   Update properties in this class or instance to reflect the simulation
    %   data and the desired plot appearance.
    %   Use the autoConfig function to automatically detect configuration
    %   based on directory structure and contents.
    
    properties
        FolderPath {mustBeFolder} = pwd
        SimulationTime {mustBePositive} = (60 * 30)
        IterationCount {mustBeNonnegative} = 0 % Default to 0 to differentiate defaults from manual settings.
        IterationCounts {mustBeNumeric} = [0,0] % Values are per-scenario
        SimulationScenarioList {mustBeText} = ''
        ScenarioLabelsList {mustBeText} = ''
        AppTypes {mustBeText} = 'ALL_TYPES'
        PlotWindowCoordinates {mustBeNumeric} = [350 60 450 450]
        HorizontalAxisLabel {mustBeText} = 'Number of Mobile Devices'
        MinimumMobileDevices {mustBeNonnegative} = 100
        MobileDeviceStep {mustBePositive} = 100
        MaximumMobileDevices {mustBePositive} = 600
        IncludeErrorBars = -1   % Default to -1 to differentiate defaults from manual settings.
        ColorPlot = -1          % Default to -1 to differentiate defaults from manual settings.
        XAxisStep {mustBePositive} = 1
        LineColors {mustBeFloat} = [0.8 0 0;0 0.15 0.6;0 0.23 0;0.6 0 0.6;0.08 0.08 0.08;0 0.8 0.8;0.8 0.4 0;0.8 0.8 0]
        LineStyleMono {mustBeText} = {'-k*','-ko','-ks','-kv','-kp','-kd','-kx','-kh'}
        LineStyleColor {mustBeText} = {':k*',':ko',':ks',':kv',':kp',':kd',':kx',':kh'}
    end
    
    methods(Static)
        function config = autoConfig()
            %AUTOCONFIG Automatically configure all settings based on presence of data files.
            %   Default FolderPath assumes simulation data 
            %   is stored in the sim_results folder.
            %   Default IterationCount is the number of copies
            %   of each output file in the sim_results folder,
            %   including all subfolders.
            %   Other data properties are likewise set according to 
            %   the file names present in the sim_results folder/subfolders.
            %   Plot details should default to something 
            %   appropriate to the data present.
            config = configuration;
            config = finishConfig(config);
        end 
    end
    
    methods       
        function newConfig = finishConfig(oldConfig)
            %FINISHCONFIG As autoConfig(), but preserve non-defaults.
            %   For example, if oldConfig.FolderPath is set, then any
            %   oldConfig properties containing the default values will be
            %   reconfigured based on the files present in the 
            %   oldConfig.FolderPath directory.
            newConfig = configuration;
            % Most important property is FolderPath, so start there.
            scriptPath = pwd;
            if strcmpi(newConfig.FolderPath, oldConfig.FolderPath)
                while 1
                    splitPath = strsplit(pwd, '\');
                    if contains(splitPath(length(splitPath)), 'PFogSim')
                        break
                    end
                    cd ../
                end
                resultsFolder = ls('sim_results');
                if ~isempty(resultsFolder)
                    dataPath = strcat(pwd, '/sim_results');
                end
                newConfig.FolderPath = dataPath;
            else
                newConfig.FolderPath = oldConfig.FolderPath;
            end
            cd(newConfig.FolderPath);
            allFiles = dir('**/*SIMRESULT_*_GENERIC*');
            cd(scriptPath);
            % Now, use the files in FolderPath to set remaining properties.
            allNames = [allFiles.name];
            %TODO: Find a way to replace NEXT_FIT in the regex to something
            %more dynamic in case "orchestrator_policies" is changed to
            %something else in the future.
            regex = 'SIMRESULT_(?<scenario>[\w_\s]+)_NEXT_FIT_(?<devices>\d+)DEVICES_(?<appType>[\w \s]+)_GENERIC';
            combos = regexp(allNames, regex, 'names');
            scenariosFullList = string({combos.scenario});
            allScenarios = unique(scenariosFullList(:));
            % Update scenario list and series labels.
            if strcmpi(newConfig.SimulationScenarioList, oldConfig.SimulationScenarioList)
                newConfig.SimulationScenarioList = allScenarios;
            end
            if strcmpi(newConfig.ScenarioLabelsList, oldConfig.ScenarioLabelsList)
                newConfig.ScenarioLabelsList = strrep(newConfig.SimulationScenarioList, '_', ' ');
            end
            devices = string({combos.devices});
            allDeviceCounts = unique(devices(:));
            sort(allDeviceCounts);
            % Update mobile device min, max, and step.
            if strcmpi(newConfig.MinimumMobileDevices, oldConfig.MinimumMobileDevices)
                newConfig.MinimumMobileDevices = str2double(allDeviceCounts(1));
            end
            if strcmpi(newConfig.MaximumMobileDevices, oldConfig.MaximumMobileDevices)
                newConfig.MaximumMobileDevices = str2double(allDeviceCounts(length(allDeviceCounts)));
            end
            deviceStep = (newConfig.MaximumMobileDevices - newConfig.MinimumMobileDevices)/(length(allDeviceCounts)-1);
            if strcmpi(newConfig.MobileDeviceStep, oldConfig.MobileDeviceStep)
                newConfig.MobileDeviceStep = deviceStep;
            end
            appTypes = string({combos.appType});
            allAppTypes = unique(appTypes(:));
            % Update list of app types.
            if strcmpi(newConfig.AppTypes, oldConfig.AppTypes)
                newConfig.AppTypes = allAppTypes;
            end
            % Set the IterationCount.
            if newConfig.IterationCount == oldConfig.IterationCount
                modIterations = mod(length(combos),length(allScenarios)*length(allDeviceCounts)*length(allAppTypes));
                if modIterations == 0
                    newConfig.IterationCount = length(combos)/(length(allScenarios)*length(allDeviceCounts)*length(allAppTypes));
                else
                    newConfig.IterationCount = 1; 
                end
            end
            %TODO: Restructure plotGenericResult() so that it uses the
            %IterationCounts property instead of IterationCount. This will
            %likely require modifications to IncludeErrorBars behavior.
            %Then remove the IterationCount property. 
            % 
            % For each simulation scenario, find the number of instances
            % of ALL_APPS files for the minimum device count. This should
            % be the number of iterations run for that scenario.
            if newConfig.IterationCounts == oldConfig.IterationCounts
                scenarioCount = length(newConfig.SimulationScenarioList);
                countArray = zeros(scenarioCount);
                filteredArray = combos(arrayfun(@(n) strcmp(n, 'ALL_APPS'), {combos.appType}));
                filteredArray = filteredArray(arrayfun(@(n) strcmp(n, string(newConfig.MinimumMobileDevices)), {filteredArray.devices}));
                for i=1:scenarioCount
                    scenario = newConfig.SimulationScenarioList(i);
                    countArray(i) = nnz(strcmp({filteredArray.scenario}, scenario));
                end
                newConfig.IterationCounts = countArray;
            end
            if newConfig.IncludeErrorBars == oldConfig.IncludeErrorBars
                if newConfig.IterationCount > 1
                    newConfig.IncludeErrorBars = 1;
                else
                    newConfig.IncludeErrorBars = 0;
                end
            end
            if newConfig.ColorPlot == oldConfig.ColorPlot
                if length(newConfig.SimulationScenarioList) > 3
                    newConfig.ColorPlot = 1;
                else
                    newConfig.ColorPlot = 0;
                end
            end
        end
    end
end

