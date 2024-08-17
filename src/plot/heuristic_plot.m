file_name = "bot.heuristics.FinalAStarDistanceHeuristic-420453914642600.csv";

path = erase(mfilename('fullpath'), "heuristic_plot");
path = erase(path, "plot\");
path = path + "bot\results\";

M = readmatrix(path+file_name);

% Plot the data
x = unique(M(:,1));
y = unique(M(:,2));
z = M(:,3);

X = zeros(height(x), height(y));
for i=1:height(x)
    for j=1:height(y)
        X(i,j) = x(i);
    end
end

Y = zeros(height(x), height(y));
for i=1:height(x)
    for j=1:height(y)
        Y(i,j) = y(j);
    end
end

Z = zeros(height(x), height(y));
for i=1:height(x)
    for j=1:height(y)
        Z(i,j) = z((i-1)*height(y) + j);
    end
end

surf(X, Y, Z);
xlabel('vx');
ylabel('vy');
zlabel('heuristic');
