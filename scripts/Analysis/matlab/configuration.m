classdef configuration
    %CONFIGURATION Access configuration parameters for plotting simulation.
    %   Update properties in this class or instance to reflect the simulation
    %   data and the desired plot appearance.
    %   Use the autoConfig function to automatically detect configuration
    %   based on directory structure and contents.
    
    properties
        FolderPath {mustBeFolder} = pwd
        SimulationTime {mustBePositive} = (60 * 30)
        IterationCount {mustBeNonnegative} = 1
        SimulationScenarioList {mustBeText} = ''
        ScenarioLabelsList {mustBeText} = ''
        AppTypes {mustBeText} = 'ALL_TYPES'
        PlotWindowCoordinates {mustBeNumeric} = [350 60 450 450]
        HorizontalAxisLabel {mustBeText} = 'Number of Mobile Devices'
        MinimumMobileDevices {mustBeNonnegative} = 100
        MobileDeviceStep {mustBePositive} = 100
        MaximumMobileDevices {mustBePositive} = 600
        IncludeErrorBars = 0
        ColorPlot = 0
        Line1Color {mustBeNumeric} = [0.8 0 0]
        Line2Color {mustBeNumeric} = [0 0.15 0.6]
        Line3Color {mustBeNumeric} = [0 0.23 0]
        Line4Color {mustBeNumeric} = [0.6 0 0.6]
        Line5Color {mustBeNumeric} = [0.08 0.08 0.08]
        Line6Color {mustBeNumeric} = [0 0.8 0.8]
        Line7Color {mustBeNumeric} = [0.8 0.4 0]
        Line8Color {mustBeNumeric} = [0.8 0.8 0]
        LineStyleMono {mustBeText} = {'-k*','-ko','-ks','-kv','-kp','-kd','-kx','-kh'}
        LineStyleColor {mustBeText} = {':k*',':ko',':ks',':kv',':kp',':kd',':kx',':kh'}
    end
    
    methods(Static)
        function config = autoConfig()
            %AUTOCONFIG Automatically detect default settings.
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
            scriptPath = pwd;
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
            config.FolderPath = dataPath;
            cd(dataPath)
            allFiles = dir('**/*SIMRESULT_*_GENERIC*');
            cd(scriptPath)
            allNames = [allFiles.name];
            regex = 'SIMRESULT_(?<scenario>[\w_\s]+)_NEXT_FIT_(?<devices>\d+)DEVICES_(?<appType>[\w \s]+)_GENERIC';
            combos = regexp(allNames, regex, 'names');
            scenarios = string({combos.scenario});
            allScenarios = unique(scenarios(:));
            config.SimulationScenarioList = allScenarios;
            config.ScenarioLabelsList = strrep(allScenarios, '_', ' ');
            devices = string({combos.devices});
            allDeviceCounts = unique(devices(:));
            sort(allDeviceCounts);
            config.MinimumMobileDevices = str2num(allDeviceCounts(1));
            config.MaximumMobileDevices = str2num(allDeviceCounts(length(allDeviceCounts)));
            deviceStep = (config.MaximumMobileDevices - config.MinimumMobileDevices)/(length(allDeviceCounts)-1);
            config.MobileDeviceStep = deviceStep;
            appTypes = string({combos.appType});
            allAppTypes = unique(appTypes(:));
            config.AppTypes = allAppTypes;
            % TODO: Determine number of iterations. Perhaps with
            % length(combos)/(length(allScenarios)*length(allDeviceCounts)*length(allAppTypes))
            % TODO: Set IncludeErrorBars = 0 if IterationCount = 1; 1 if
            % IterationCount > 1.
            % TODO: Set ColorPlot = 0 if IterationCount < 4; 1 if
            % IterationCount >= 4.
        end 
    end
    
    methods       
        function obj = untitled(inputArg1,inputArg2)
            %UNTITLED Construct an instance of this class
            %   Detailed explanation goes here
            obj.Property1 = inputArg1 + inputArg2;
        end
        
        function outputArg = method1(obj,inputArg)
            %METHOD1 Summary of this method goes here
            %   Detailed explanation goes here
            outputArg = obj.Property1 + inputArg;
        end
    end
end

