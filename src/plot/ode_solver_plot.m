clear variables;

file_name = "solvers-3840328294400.csv";

path = erase(mfilename('fullpath'), "ode_solver_plot");
path = erase(path, "plot");
path = path + "\physics\results\";

M = readmatrix(path+file_name);

disp(M);

h_normal = M(3:end, 1);
h_log = M(3:end, 2);

tiledlayout(1, 2);
plt1 = nexttile;
% Plot regular error
euler_e_normal = M(3:end, 3);
rk2_e_normal = M(3:end, 7);
rk4_e_normal = M(3:end, 11);
plot(h_normal, euler_e_normal); hold on;
plot(h_normal, rk2_e_normal);
plot(h_normal, rk4_e_normal); 
title(plt1, 'regular plot');
grid(plt1, 'on'); 
xlabel('h');
ylabel('error');
legend('euler', 'rk2', 'rk4', 'Location', 'northwest');hold off;

plt2 = nexttile;
% Plot log log error
euler_e_log = M(3:end, 4);
rk2_e_log = M(3:end, 8);
rk4_e_log = M(3:end, 12);
plot(h_log, euler_e_log); hold on;
plot(h_log, rk2_e_log);
plot(h_log, rk4_e_log); 
title(plt2, 'log-log plot');
grid(plt2, 'on'); 
xlabel('log(h)');
ylabel('log(error)');
legend('euler', 'rk2', 'rk4', 'Location', 'southeast');hold off;