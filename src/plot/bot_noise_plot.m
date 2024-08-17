file_name = "AdaptiveHC423664442609200.csv";

path = erase(mfilename('fullpath'), "bot_noise_plot");
path = erase(path, "plot\");
path = path + "bot\results\";

M = readmatrix(path+file_name);

% Plot the data
noise = M(:,1);
average_accuracy = M(:,2);
average_distance = M(:,3);

plot(noise, average_accuracy); hold on;
plot(noise, average_distance);
xlabel('noise');
zlabel('heuristic'); hold off;
