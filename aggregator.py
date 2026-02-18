import os
import argparse
import re

# --- Расширенные настройки ---
TEXT_EXTENSIONS = {
    # Common & Android
    '.kt', '.java', '.xml', '.gradle', '.kts', '.toml', '.pro', '.properties',
    # iOS / Apple
    '.swift', '.h', '.m', '.mm', '.podspec', '.json',
    # Web / KMP Common
    '.js', '.ts', '.html', '.css'
}

BLACKLIST_FILES = {
    'local.properties', 'ExampleUnitTest.kt', 'ExampleInstrumentedTest.kt',
    'gradlew', 'gradlew.bat', 'README.md', 'LICENSE', 'CONTRIBUTING.md',
    'Podfile.lock', 'Package.resolved', '.DS_Store'
}

# Игнорируемые директории для всех платформ
IGNORE_DIRS = {
    'build', '.git', '.gradle', '.idea', 'captures', 'outputs', 'mipmap',
    'node_modules', '.xcodeproj', '.xcworkspace', 'DerivedData', 'Pods', '.swiftpm'
}

# Приоритетные конфиги для разных платформ
PRIORITY_FILES = [
    'libs.versions.toml', 'settings.gradle.kts', 'build.gradle.kts', # KMP/Android
    'Podfile', 'Package.swift' # iOS
]

ARCHITECTURE_PROMPT = """
> ### 🧠 MULTIPLATFORM CONTEXT FOR LLM
> This project is a Multiplatform (Android, iOS, KMP) application.
> 1. **Priority:** Build configs and dependency managers are provided first.
> 2. **Structure:** Logic (Kotlin, Swift) is prioritized over resources.
> 3. **Optimization:** Comments in XML/Swift/Kotlin and heavy binary/vector data are skipped.
"""

def clean_comments(content, ext):
    """Удаляет комментарии в зависимости от расширения файла."""
    if ext == '.xml':
        return re.sub(r'', '', content, flags=re.DOTALL).strip()
    elif ext in {'.kt', '.kts', '.swift', '.java', '.h', '.m'}:
        # Удаляет однострочные // и многострочные /* */ комментарии
        content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
        content = re.sub(r'//.*', '', content)
        return content.strip()
    return content.strip()

def is_heavy_resource(file_path):
    """Проверяет, является ли файл тяжелым ресурсом (вектор или бинарник)."""
    ext = os.path.splitext(file_path)[1].lower()
    if ext == '.xml':
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                header = f.read(500)
                return '<vector' in header or '<shape' in header
        except: return False
    # Игнорируем сложные Storyboard и Asset-каталоги в виде текста
    if ext in {'.storyboard', '.xib', '.xcassets'}:
        return True
    return False

def should_include_file(file_name):
    ext = os.path.splitext(file_name)[1].lower()
    return ext in TEXT_EXTENSIONS and file_name not in BLACKLIST_FILES

def generate_filtered_tree(project_abs_path):
    tree = []
    for root, dirs, files in os.walk(project_abs_path):
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS and not d.endswith(('.xcodeproj', '.xcworkspace', '.xcassets'))]
        valid_files = [f for f in files if should_include_file(f)]
        if valid_files or any(should_include_file(f) for r, d, fs in os.walk(root) for f in fs):
            rel_root = os.path.relpath(root, project_abs_path)
            level = 0 if rel_root == "." else rel_root.count(os.sep) + 1
            name = os.path.basename(project_abs_path) if rel_root == "." else os.path.basename(root)
            indent = "|   " * (level - 1) if level > 0 else ""
            prefix = "|-- " if level > 0 else ""
            tree.append(f"{indent}{prefix}{name}/")
            sub_indent = "|   " * level
            for f in valid_files:
                tag = " [Resource/Heavy]" if is_heavy_resource(os.path.join(root, f)) else ""
                tree.append(f"{sub_indent}|-- {f}{tag}")
    return "\n".join(tree)

class ContextWriter:
    def __init__(self, base_name, split, max_kb, project_tree):
        self.base_name = base_name
        self.split = split
        self.max_bytes = max_kb * 1024
        self.project_tree = project_tree
        self.part_number = 1
        self.buffer = []
        self.buffer_size = 0

    def add_file(self, rel_path, content, lang):
        file_header = f"### 📄 File: {rel_path}\n```{lang}\n"
        full_entry = f"{file_header}{content}\n```\n"
        entry_size = len(full_entry.encode('utf-8'))
        if self.split and self.buffer_size + entry_size > self.max_bytes and self.buffer:
            self.flush()
        self.buffer.append(full_entry)
        self.buffer_size += entry_size

    def flush(self):
        if not self.buffer: return
        suffix = f"_part_{self.part_number}" if self.split else ""
        out_name = self.base_name if self.base_name.endswith('.md') else f"{self.base_name}.md"
        if self.split: out_name = out_name.replace('.md', f"{suffix}.md")
        with open(out_name, 'w', encoding='utf-8') as f:
            f.write(f"# Multiplatform Context{suffix}\n{ARCHITECTURE_PROMPT}\n## 📂 Tree\n```text\n{self.project_tree}\n```\n---\n## 🧩 Content\n")
            for entry in self.buffer: f.write(entry)
        print(f"✅ Готово: {out_name}")
        self.part_number += 1
        self.buffer = []
        self.buffer_size = 0

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('path', nargs='?', default='.')
    parser.add_argument('--split', action='store_true')
    parser.add_argument('--max-size', type=int, default=600)
    parser.add_argument('-o', '--output', type=str, default='multiplatform_context')
    args = parser.parse_args()

    project_abs_path = os.path.abspath(args.path)
    full_tree = generate_filtered_tree(project_abs_path)
    writer = ContextWriter(args.output, args.split, args.max_size, full_tree)

    all_files = []
    for root, dirs, files in os.walk(project_abs_path):
        dirs[:] = [d for d in dirs if d not in IGNORE_DIRS]
        for f in files:
            if should_include_file(f):
                all_files.append(os.path.join(root, f))

    def sort_key(f_path):
        f_name = os.path.basename(f_path)
        if f_name in PRIORITY_FILES: return (0, f_name)
        ext = os.path.splitext(f_name)[1].lower()
        if ext in {'.kt', '.swift', '.java'}: return (1, f_path)
        return (2, f_path)

    all_files.sort(key=sort_key)

    lang_map = {
        '.kt': 'kotlin', '.kts': 'kotlin', '.swift': 'swift',
        '.java': 'java', '.xml': 'xml', '.toml': 'toml',
        '.h': 'objectivec', '.m': 'objectivec', '.mm': 'objectivec'
    }

    for file_path in all_files:
        rel_path = os.path.relpath(file_path, project_abs_path)
        ext = os.path.splitext(file_path)[1].lower()
        is_heavy = is_heavy_resource(file_path)
        lang = lang_map.get(ext, 'text')

        try:
            with open(file_path, 'r', encoding='utf-8') as f_in:
                if is_heavy:
                    content = f""
                else:
                    raw_content = f_in.read()
                    content = clean_comments(raw_content, ext)
                writer.add_file(rel_path, content, lang)
        except: pass
    writer.flush()

if __name__ == "__main__":
    main()
