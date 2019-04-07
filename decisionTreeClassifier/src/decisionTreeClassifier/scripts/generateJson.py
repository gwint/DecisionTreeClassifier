from json import dumps

def main():
    classes_filename = "/home/gregory/DecisionTreeClassifier/decisionTreeClassifier/classes.txt"
    features_filename = "/home/gregory/DecisionTreeClassifier/decisionTreeClassifier/data.txt"

    json_obj = {}

    with open(classes_filename, 'r') as classes_file_obj:
        num_classes = 0
        class_label = classes_file_obj.readline()
        class_labels = []
        while class_label:
            num_classes += 1
            class_labels.append(float(class_label))
            class_label = classes_file_obj.readline()
        json_obj["num_items"] = num_classes
        json_obj["class_labels"] = class_labels[:3]

    with open(features_filename, 'r') as features_file_obj:
        all_features = []
        features = features_file_obj.readline()
        while features:
            sample_features = list(map(float, features.split(",")))
            all_features += sample_features
            features = features_file_obj.readline()
        json_obj["all_features"] = all_features[:3]

    json_obj["test_samples"] = [1,2,3,4,5]

    target_file_obj = open("copy.json", 'w')
    target_file_obj.write(dumps(json_obj) + '\n')
    target_file_obj.close()

main()
