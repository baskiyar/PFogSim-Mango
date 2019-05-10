function [] = plotTaskFailureReason()

    %---- Ignore this script.

    %{
    plotGenericResult(1, 10, 'Failed Task due to VM Capacity (%)', 'ALL_APPS', 1);
    plotGenericResult(1, 10, {'Failed Task due to VM Capacity';'for Augmented Reality App (%)'}, 'AUGMENTED_REALITY', 1);
    plotGenericResult(1, 10, {'Failed Task due to VM Capacity';'for Health App (%)'}, 'HEALTH_APP', 1);
    plotGenericResult(1, 10, {'Failed Task due to VM Capacity';'for Infotainment App (%)'}, 'INFOTAINMENT_APP', 1);
    plotGenericResult(1, 10, {'Failed Task due to VM Capacity';'for Heavy Computation App (%)'}, 'HEAVY_COMP_APP', 1);
    %}

    %plotGenericResult(1, 11, 'Average Failed Task due to Mobility (%)', 'ALL_APPS', 1);
    %plotGenericResult(1, 11, {'Failed Task due to Mobility';'for Augmented Reality App (%)'}, 'AUGMENTED_REALITY', 1);
    %plotGenericResult(1, 11, {'Failed Task due to Mobility';'for Health App (%)'}, 'HEALTH_APP', 1);
    %plotGenericResult(1, 11, {'Failed Task due to Mobility';'for Infotainment App (%)'}, 'INFOTAINMENT_APP', 1);
    %plotGenericResult(1, 11, {'Failed Task due to Mobility';'for Heavy Computation App (%)'}, 'HEAVY_COMP_APP', 1);
    
end
