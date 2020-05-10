# Неокогнитрон

## Описание структуры

#### Количество стадий равняется четырём, то есть существует четыре пары s-слой + c-слой
#### Количество плоскостей в каждой стадии (_число s-плоскостей равняется числу c-плоскостей_)

| **Стадия 1** | **Стадия 2** | **Стадия 3** | **Стадия 4** |
| :---: | :---:| :---: | :---: |
| 16 | 16 | 16 | 9 |

#### Размер s-плоскостей в каждой стадии

| **Стадия 1** | **Стадия 2** | **Стадия 3** | **Стадия 4** |
| :---: | :---:| :---: | :---: |
| 28x28 | 20x20 | 12x12 | 10x10 |

#### Размер c-плоскостей в каждой стадии

| **Стадия 1** | **Стадия 2** | **Стадия 3** | **Стадия 4** |
| :---: | :---:| :---: | :---: |
| 20x20 | 12x12 | 10x10 | 1x1 |

#### Размер рецептивных полей s-слоя в каждой стадии

| **Стадия 1** | **Стадия 2** | **Стадия 3** | **Стадия 4** |
| :---: | :---:| :---: | :---: |
| 7x7 | 5x5 | 3x3 | 3x3 |

#### Размер рецептивных полей c-слоя в каждой стадии

| **Стадия 1** | **Стадия 2** | **Стадия 3** | **Стадия 4** |
| :---: | :---:| :---: | :---: |
| 7x7 | 5x5 | 3x3 | 9x9 |

#### Порог чувствительности для каждого s-слоя

| **S-слой 1** | **S-слой 2** | **S-слой 3** | **S-слой 4** |
| :---: | :---:| :---: | :---: |
| 0.55 | 0.51 | 0.58 | 0.3 |

#### Скорость обучения для каждого s-слоя

| **S-слой 1** | **S-слой 2** | **S-слой 3** | **S-слой 4** |
| :---: | :---:| :---: | :---: |
| 100 | 100 | 100 | 100 |

### В каждом s-слое присутствует в единственном количестве _плоскость тормозящих ячеек_, её размерность равняется размерности s-плоскостей

## Реализация

### Неокогнитрон реализован на Java

### Для инициализации весов _c-связей_ и _d-связей_ используется вейвлет мексиканская шляпа с пиком в -1, где t^2 = x^2 + y^2
```java
double[][] initialize_mexican_hat_weights() {
        double[][] weights = new double[receptive_field_size][receptive_field_size];
        int half_size = receptive_field_size / 2;
        for (int i = -half_size; i <= half_size; i++) {
            for (int j = -half_size; j <= half_size; j++) {
                double squared_t = i * i + j * j;
                weights[i + half_size][j + half_size] = (squared_t - 1) * Math.exp(-squared_t / 2);
            }
        }
        return weights;
    }
```

### Абстрактный класс для слоя (имплементирует интерфейс Serializable для последующей записи объекта неокогнитрона в файл, как и остальные входящие в его поля классы)
```java
abstract class Layer implements Serializable {
    private static final long serialVersionUID = 9L;

    int planes_amount;
    int plane_size;
    int receptive_field_size;

    Layer(int planes_amount, int plane_size, int receptive_field_size) {
        this.planes_amount = planes_amount;
        this.plane_size = plane_size;
        this.receptive_field_size = receptive_field_size;
    }
    <...>        
    abstract void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train);
    abstract double[][] get_v_output();
    abstract double[][][] get_s_output();
    abstract double[][][] get_c_output();
}
```

### Класс s-слоя
```java
class Simple_Layer extends Layer implements Serializable {
    private static final long serialVersionUID = 2L;

    Simple_Cell[][][] simple_cells;
    double[][] c_weights;
    double[] b_weights;
    double q;
    double θ;

    Simple_Layer(int planes_amount, int simple_plane_size, int receptive_field_size, int previous_layer_planes_amount, double θ, double q) {
        super(planes_amount, simple_plane_size, receptive_field_size);
        c_weights = initialize_mexican_hat_weights();
        simple_cells = new Simple_Cell[planes_amount][simple_plane_size][simple_plane_size];
        for (int i = 0; i < planes_amount; i++) {
            for (int j = 0; j < simple_plane_size; j++) {
                for (int l = 0; l < simple_plane_size; l++) {
                    simple_cells[i][j][l] = new Simple_Cell(previous_layer_planes_amount, receptive_field_size);
                }
            }
        }
        b_weights = new double[planes_amount];
        this.θ = θ;
        this.q = q;
    }
}
```

### Класс c-слоя
```java
class Complex_Layer extends Layer implements Serializable {
    private static final long serialVersionUID = 3L;

    double[][] d_weights;
    double[][][] c_output;

    Complex_Layer(int planes_amount, int complex_plane_size, int receptive_field_size) {
        super(planes_amount, complex_plane_size, receptive_field_size);
        d_weights = initialize_mexican_hat_weights();
        c_output = new double[planes_amount][plane_size][plane_size];
    }
}
```

### Класс v-слоя
```java
class Inhibitory_Layer extends Layer implements Serializable{
    private static final long serialVersionUID = 4L;

    double[][] v_output;
    double[][] c_weights;

    Inhibitory_Layer(int inhibitory_plane_size, int receptive_field_size) {
        super(1, inhibitory_plane_size, receptive_field_size);
        v_output = new double[plane_size][plane_size];
        c_weights = initialize_mexican_hat_weights();
    }
}
```

### Вычисление выходных значений v-ячейки (тормозящей ячейки)
```java
double evaluate_behaviour(double[][][] image, int shift_x, int shift_y) {
        double value = 0;
        for (int k = 0; k < image.length; k++) {
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    double image_value = 0;
                    try {
                        image_value = image[k][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                    } catch (ArrayIndexOutOfBoundsException ignored) {}

                    value += c_weights[x][y] * image_value * image_value;
                }
            }
        }
        return Math.sqrt(value);
    }
```

### Вычисление выходных значений v-плоскости (тормозящей плоскости)
```java
 @Override
    void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train) {
        for (int i = 0; i < plane_size; i++) {
            for (int j = 0; j < plane_size; j ++) {
                double value = evaluate_behaviour(image, i, j);
                this.v_output[i][j] = Double.isNaN(value) ? 0 : value;
            }
        }
    }
```

### Вычисление delta a-связей по правилу Хебба
```java
void hebb_rule_delta_a_evaluation(double[][][] image, int shift_x, int shift_y, int plane_number) {
        for (int k = 0; k < image.length; k++) {
            double[][] delta_a = new double[receptive_field_size][receptive_field_size];
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    double image_value = 0;
                    try {
                        image_value = image[k][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                    } catch (ArrayIndexOutOfBoundsException ignored) {}
                    delta_a[x][y] = q * c_weights[x][y] * image_value;
                }
            }
            simple_cells[plane_number][shift_x][shift_y].change_a_weights(delta_a, k);

            double[][][] changed_a_weights = simple_cells[plane_number][shift_x][shift_y].get_a_weights();
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    simple_cells[plane_number][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2].set_a_weights(changed_a_weights);
                }
            }
        }
    }
```

### Вычисление b-связей по правилу Хебба для плоскости
```java
 double hebb_rule_b_evaluation(int plane_number, int x, int y) {
        double value = simple_cells[plane_number][x][y].evaluate_b_weights(c_weights);
        return Double.isNaN(value) ? 0 : value;
    }

<...>

double evaluate_b_weights(double[][] c_weights) {
        double value = 0;
        for (int k = 0; k < a_weights.length; k++) {
            for (int i = 0; i < a_weights[0].length; i++) {
                for (int j = 0; j < a_weights[0][0].length; j++) {
                    if (c_weights[i][j] == 0) {
                        continue;
                    }
                    value += a_weights[k][i][j] * a_weights[k][i][j] / c_weights[i][j];
                }
            }
        }
        return Math.sqrt(value);
    }
```

### Вычисление выходных значений s-ячейки (свёртка) 
```java
void evaluate_behaviour(double[][][] image, int shift_x, int shift_y, int plane_number, double[][] v_output) {
        double sum = 0;
        for (int k = 0; k < image.length; k++) {
            double[][] image_window = new double[receptive_field_size][receptive_field_size];
            for (int x = 0; x < receptive_field_size; x++) {
                for (int y = 0; y < receptive_field_size; y++) {
                    image_window[x][y] = 0;
                    try {
                        image_window[x][y] = image[k][shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                    } catch (ArrayIndexOutOfBoundsException ignored) {}
                }
            }
            sum += simple_cells[plane_number][shift_x][shift_y].multiply(image_window, k);
        }

        sum = (1 + sum) / (1 + θ * b_weights[plane_number] * v_output[shift_x][shift_y]) - 1;

        if (sum < 0) {
            sum = 0;
        }

        sum *= θ / (1 - θ);

        simple_cells[plane_number][shift_x][shift_y].set_output(sum);
    }

<...>

double multiply(double[][] image_window, int plane_number) {
        double value = 0;
        for (int i = 0; i < a_weights[plane_number].length; i++) {
            for (int j = 0; j < a_weights[plane_number][0].length; j++) {
                value += a_weights[plane_number][i][j] * image_window[i][j];
            }
        }
        return value;
    }
```

### Вычисление выходных значений s-плоскости (если установлен флаг обучения, то происходит непосредственное обучение)

```java
@Override
    void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train) {
        int starting_point = receptive_field_size / 2;
        int amount = plane_size / receptive_field_size;

        if (train) {
            int plane_number = 0;
            for (int i = starting_point; i <= starting_point + receptive_field_size * (amount - 1); i += receptive_field_size) {
                for (int j = starting_point; j <= starting_point + receptive_field_size * (amount - 1); j += receptive_field_size) {
                    hebb_rule_delta_a_evaluation(image, i, j, plane_number);
                    b_weights[plane_number] = hebb_rule_b_evaluation(plane_number, i, j);
                    plane_number++;
                }
            }
        }

        for (int k = 0; k < simple_cells.length; k++) {
            for (int i = 0; i < plane_size; i++) {
                for (int j = 0; j < plane_size; j++) {
                    evaluate_behaviour(image, i, j, k, v_output);
                }
            }
        }

        for (int k = 0; k < simple_cells.length; k++) {
            for (int i = 0; i < plane_size; i++) {
                for (int j = 0; j < plane_size; j++) {
                }
            }
        }
    }
```

### Вычисление выходных значений c-ячейки (пулинг)

```java
void make_pooling(double[][] image, int plane_number, int shift_x, int shift_y, int shift) {
        double value = 0;
        for (int x = 0; x < receptive_field_size; x++) {
            for (int y = 0; y < receptive_field_size; y++) {
                // observing floating window cell value
                double image_value = 0;
                try {
                    image_value = image[shift_x + x - receptive_field_size / 2][shift_y + y - receptive_field_size / 2];
                } catch (ArrayIndexOutOfBoundsException ignored) {}

                value += d_weights[x][y] * image_value;
            }
        }

        value = (value < 0 ? 0 : value) / (1 + (value < 0 ? 0 : value));
        c_output[plane_number][shift_x - shift][shift_y - shift] = value;
    }
```

### Вычисление выходных значений c-плоскости

```java
 @Override
    void propagate(double[][][] image, double[][] v_output, double[][][] s_output, boolean train) {
        int shift = (s_output[0].length - plane_size) / 2;

        for (int k = 0; k < s_output.length; k++) {
            for (int i = 0; i < plane_size; i++) {
                for (int j = 0; j < plane_size; j++) {
                    make_pooling(s_output[k], k, i + shift, j + shift, shift);
                }
            }
        }
    }
```

### Непосредственное считывание изображения и его подача в неокогнитрон (процесс обучения), сериализация неокогнитрона в файл
 
```java
BufferedImage image = null;

Neocognitron neocognitron = new Neocognitron();

boolean train = true;
for (int k = 1; k <= 9; k++) {
    for (int i = 0; i < 500; i++) {
        image = ImageIO.read(new File("train_images/" + k + "_" + i + ".jpg"));
        neocognitron.set_image(image);
        neocognitron.proceed(train, k - 1);
    }
}

neocognitron.evaluate_prediction();
ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("structure/neocognitron"));
objectOutputStream.writeObject(neocognitron);
objectOutputStream.close();
```

### Десериализация объекта неокогнитрона и распознавание изображений без обучения
```java
ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("structure/neocognitron"));

Neocognitron deserialized_neocognitron = (Neocognitron) objectInputStream.readObject();

BufferedImage image = null;
boolean train = false;
for (int k = 1; k <= 9; k++) {
    for (int i = 0; i < 50; i++) {
        image = ImageIO.read(new File("test_images/" + k + "_" + i + ".jpg"));
        deserialized_neocognitron.set_image(image);
        deserialized_neocognitron.proceed(train, k);
    }
}
```

## Обучение неокогнитрона

### Обучение производилось на 500 картинках для каждой цифры, итого - 4500 изображений из директории train_images

### Процесс непосредственного обучения можно увидеть на нижепредставленной GIF
![](https://github.com/beryanow/neural_networks_labs/blob/master/Lab_4%20(Neocognitron)/assistance/Work%20Process.gif)

## Тестирование неокогнитрона

### Тестирование производилось на 50 картинках для каждой цифры, итого - 450 изображений из директории test_images

### Матрица неточностей

#### Строки матрицы соответствуют predicted значениям, столбцы соответствуют real значениям 

 | Цифра | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 
 | :---: | :---:| :---: | :---: | :---: | :---:| :---: | :---: | :---: | :---:|
 | 1 | 29 | 0 | 0 | 5 | 0 | 0 | 14 | 0 | 2 | 
 | 2 | 0 | 50 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 
 | 3 | 0 | 0 | 21 | 0 | 0 | 0 | 25 | 0 | 4 | 
 | 4 | 10 | 0 | 0 | 7 | 16 | 0 | 0 | 0 | 17 | 
 | 5 | 0 | 1 | 0 | 0 | 45 | 0 | 0 | 4 | 0 | 
 | 6 | 0 | 0 | 3 | 0 | 0 | 11 | 1 | 35 | 0 | 
 | 7 | 5 | 0 | 0 | 0 | 0 | 0 | 45 | 0 | 0 | 
 | 8 | 0 | 0 | 1 | 0 | 0 | 0 | 0 | 49 | 0 | 
 | 9 | 3 | 0 | 9 | 1 | 0 | 5 | 0 | 20 | 12 | 
 
 ### Полнота и точность для каждого класса цифры
 | 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 
 | :---: | :---:| :---: | :---: | :---: | :---:| :---: | :---: | :---: |
 | Полнота | 0,617021277 | 0,980392157 | 0,617647059 | 0,538461538 | 0,737704918 | 0,6875 | 0,529411765 | 0,453703704 | 0,342857143 | 
 | Точность | 0,58 | 1 | 0,42 | 0,14 | 0,9 | 0,22 | 0,92 | 0,98 | 0,24 |

### Общая полнота и точность классификатора
| | |
| :---: | :---:| 
 | Полнота классификатора | 0,611633284 | 
 | Точность классификатора | 0,597777778 | 
 
 ### ROC-кривая
 
 ![roc curve](https://github.com/beryanow/neural_networks_labs/blob/master/Lab_4%20(Neocognitron)/assistance/ROC.png)
 ### AUC-значение
 #### AUC = 0,623980267  
 




