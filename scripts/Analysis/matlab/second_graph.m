function [] = second_graph()
%
% Please change the value of startOfMobileDeviceLoop, stepOfMobileDeviceLoop, and endOfMobileDeviceLoop.
% @author Qian Wang
% @since 1.0.0
% @see dependencies
%
    startOfMobileDeviceLoop = 1000;
    stepOfMobileDeviceLoop = 1000;
    endOfMobileDeviceLoop = 5000;
    for i = startOfMobileDeviceLoop:stepOfMobileDeviceLoop:endOfMobileDeviceLoop
        %disp(i);
        first_graph(i);
    end
end