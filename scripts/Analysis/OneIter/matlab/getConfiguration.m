%--------------------------------------------------------------
% pFog-Sim vs Centralized Orchestrator
% returns a value according to the given arguments
%--------------------------------------------------------------
function [ret_val] = getConfiguration(argType)
    if(argType == 1)
        ret_val = 'S:\Shehenaz\FIaaSRSM-SampleTests\Run-82-All';
        % USERNAME = your username
        % FILE = File or path that your EdgeCloudSim-master folder is located in
        
    elseif(argType == 2)
        ret_val = 60 * 125; %simulation time (in seconds)
    elseif(argType == 3)
        ret_val = 1; %Number of iterations
    elseif(argType == 4)
        ret_val = 1; %x tick interval for number of mobile devices
    elseif(argType == 5)
        %ret_val = {'CENTRALIZED_ORCHESTRATOR','EDGE_ONLY','PUDDLE_ORCHESTRATOR','LOCAL','CLOUD_ONLY'};
        ret_val = {'SELECTED_LEVELS','CENTRALIZED_ORCHESTRATOR','EDGE_BY_LATENCY','EDGE_BY_DISTANCE','LOCAL_ONLY','FIXED_NODE','SELECTED_NODES','CLOUD_ONLY'};
    elseif(argType == 6)
        %ret_val = {'Centralized','Edge Only','Fog Distance Only','Local','Cloud'};
        ret_val = {'Selected_Levels','Centralized','Edge_by_Latency','Edge_by_Distance','Local_only','Fixed_Node','Selected_Nodes','Cloud_only'};
    elseif(argType == 7)
        ret_val=[350 60 450 450]; %position of figure
    elseif(argType == 8)
        ret_val = 20; %server load log interval (in seconds)
    elseif(argType == 9)
        ret_val = 'Number of Mobile Devices'; %Common text for s axis
    elseif(argType == 10)
        ret_val = 100; %min number of mobile device
    elseif(argType == 11)
        ret_val = 100; %step size of mobile device count
    elseif(argType == 12)
        ret_val = 100; %max number of mobile device
    elseif(argType == 19)
        ret_val = 0; %return 1 if you want to plot errors -- shaik set to 0
    elseif(argType == 20)
        ret_val=1; %return 1 if graph is plotted colerful
    elseif(argType == 21)
        ret_val=[0.8 0 0]; %color of first line
    elseif(argType == 22)
        ret_val=[0 0.15 0.6]; %color of second line
    elseif(argType == 23)
        ret_val=[0 0.23 0]; %color of third line
    elseif(argType == 24)
        ret_val=[0.6 0 0.6]; %color of fourth line
    elseif(argType == 25)
        ret_val=[0.08 0.08 0.08]; %color of fifth line
    elseif(argType == 26)
        ret_val=[0 0.8 0.8]; %color of sixth line
    elseif(argType == 27)
        ret_val=[0.8 0.4 0]; %color of seventh line
    elseif(argType == 28)
        ret_val=[0.8 0.8 0]; %color of eighth line
    elseif(argType == 40)
        ret_val={'-k*','-ko','-ks','-kv','-kp','-kd','-kx','-kh'}; %line style (marker) of the colerless line
    elseif(argType == 50)
        ret_val={':k*',':ko',':ks',':kv',':kp',':kd',':kx',':kh'}; %line style (marker) of the colerfull line
    end
end
