from sys import argv, stderr, exit

def main():
    if len(argv) < 3:
        print("Both old classes filename and new classes filename must be provided", file=stderr)
        exit(1)

    old_classes_file_name = argv[1]
    new_classes_file_name = argv[2]

    with open(old_classes_file_name, 'r') as old_classes_file_obj, \
         open(new_classes_file_name, "w") as new_classes_file_obj:
        class_label = old_classes_file_obj.readline().strip()
        while class_label:
            saved_label = class_label.strip()
            class_label = old_classes_file_obj.readline().strip()
            moved_label = saved_label+"," if class_label else saved_label
            new_classes_file_obj.write(moved_label)

    return 0


main()
