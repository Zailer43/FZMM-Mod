import os
import shutil

lang = {
  "de_de": {"de_de"},
  "ru_ru": {"ru_ru"},
  "es_es": {"es_ar", "es_cl", "es_ec", "es_es", "es_mx", "es_uy", "es_ve"}
}

src_dir = "src/main/resources/assets/fzmm/lang/"

# Iterate through and clone all language variants
for lang_code, variants in lang.items():

    # Read the JSON file of the language
    lang_file = os.path.join(src_dir, f"{lang_code}.json")
    with open(lang_file, "r") as f:
        lang_data = f.read()

    for variant in variants:
        # Write the JSON file of the variant
        variant_file = os.path.join(src_dir, f"{variant}.json")
        with open(variant_file, "w") as f:
            f.write(lang_data)
